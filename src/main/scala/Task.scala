import java.nio.file.{Path, Paths, Files}
import java.io._
import scala.io.Source
import grizzled.slf4j.Logging

/**
 *  A collection of tasks.
 *
 *  Corresponds to a directory.
 *  Each task is in a subdirectory.
 */
object TaskCollection {
  val lastIdFilename = "lastId.txt"
}

class TaskCollection(val directory: Path) {

  def incrNextId(): Integer = {
    val p = directory.resolve(TaskCollection.lastIdFilename)
    val f = p.toFile
    val nextId: Integer = if (Files.exists(p)) {
      val lines = Source.fromFile(f).getLines()
      lines.next().toInt + 1
    } else {0}
    val pw = new PrintWriter(f)
    try pw.write(nextId.toString) finally pw.close()
    nextId
  }

  def getTasks: Iterable[Task] = {
    val subdirs: Iterable[File] = directory.toFile.listFiles().
      filter(_.isDirectory).
      filter(_.getName.startsWith("task_"))
    val subdirpaths: Iterable[Path] = subdirs.map(x => Paths.get(x.getName))
    subdirpaths.map(x => new Task(x))
  }
}

/**
 *  An external process that we run.
 *
 *  Each task has it's own directory created for it.
 *  This directory contains the following files:
 *   - state.txt - either NOT_STARTED, RUNNING, FINISHED_OK, FINISHED_ERROR.
 */
object Task {

  val stateFilename = "state.txt"
  val descriptionFilename = "description.txt"

  object TaskStates extends Enumeration {
    val NotStarted = Value("NOT_STARTED")
    val Running = Value("RUNNING")
    val FinishedOK = Value("FINISHED_OK")
    val FinishedError = Value("FINISHED_ERROR")
  }
  type TaskState = TaskStates.Value

  def setStateHelper(directory: Path, state: TaskState) = {
    val pw = new PrintWriter(directory.resolve(stateFilename).toFile)
    try pw.write(state.toString) finally pw.close()
  }
  def setDescriptionHelper(directory: Path, description: String) = {
    val pw = new PrintWriter(directory.resolve(descriptionFilename).toFile)
    try pw.write(description) finally pw.close()
  }

  
  /**
    * Create a new task.  Mostly just setting the database entry up,
    * creating the directory and stuff like that.  Subclasses of this
    *    do the real work.
    *    
    * @param parentDirectory The directory in which we place create the
    *            tasks directory.
    * @param description Describes the task.
    */
  def createDirectory(parentDirectory: Path, description: String = ""): Path = {
    if (!Files.exists(parentDirectory)) {
      throw new Exception(s"Parent directory of task $parentDirectory does not exist")
    }
    val id = new TaskCollection(parentDirectory).incrNextId()
    val dn = s"task_$id"
    val directory = parentDirectory.resolve(dn)
    directory.toFile.mkdir()
    setStateHelper(directory, TaskStates.NotStarted)
    setDescriptionHelper(directory, description)
    directory
  }

  def create(parentDirectory: Path, description: String = ""): Task = {
    val directory = createDirectory(parentDirectory, description)
    new Task(directory)
  }
}

class Task(val directory: Path) {
        
  def statePath: Path = { directory.resolve(Task.stateFilename) }
  def descriptionPath: Path = { directory.resolve(Task.descriptionFilename) }

  def setState(state: Task.TaskState) = {
    Task.setStateHelper(directory, state)
  }

  def getState: Task.TaskState = {
    val lines = Source.fromFile(statePath.toFile).getLines()
    Task.TaskStates.withName(lines.next())
  }

  def setDescription(description: String) = {
    Task.setDescriptionHelper(directory, description)
  }

  def getDescription: String = {
    Source.fromFile(descriptionPath.toFile).getLines().mkString("\n")
  }

  if (!Files.exists(directory)) {
    throw new Exception(s"Task's directory does not exist: $directory")
  }
  val description = getDescription

  def waitUntilTrue(pollInterval: Long, test: () => Boolean) = {
    while (!test()) {
      Thread.sleep(pollInterval)
    }
  }

  def isFinished = {
    val state = getState
    Set(Task.TaskStates.FinishedOK,
      Task.TaskStates.FinishedError).contains(state)
  }

  def waitUntilFinished(pollInterval: Long = 1000) = {
    waitUntilTrue(pollInterval, () => isFinished)
  }

}

object TemplateHelper extends Logging{

  def formatString(values: Map[String, String], text: String): String = {
    var updated = text
    values.foreach {
      case (key, value) =>
        updated = updated.replace(s"@$key@", value)
    }
    updated
  }
  
  def formatTemplate(values: Map[String, String], templatePath: Path,
		     outputPath: Path) = {
    val template = Source.fromFile(templatePath.toFile).getLines().mkString("\n")
    val formatted = formatString(values, template)
    val pw = new PrintWriter(outputPath.toFile)
    try pw.write(formatted) finally pw.close()
  }
}


object VivadoTask extends Logging {

  class MessageType(
    val name: String,
    val logFunction: (=> Any) => Unit) {
  }

  val debugMT = new MessageType("DEBUG", debug)
  val infoMT = new MessageType("INFO", info)
  val warningMT = new MessageType("WARNING", warn)
  val criticalWarningMT = new MessageType("CRITICAL_WARNING", error)
  val errorMT = new MessageType("ERROR", error)
  val fatalErrorMT = new MessageType("FATAL_ERROR", error)
  val messageTypes = Set(debugMT, infoMT, warningMT, criticalWarningMT, errorMT,
			 fatalErrorMT)
  val errorMessageTypes = Set(criticalWarningMT, errorMT, fatalErrorMT)

  object Message {
    def fromLine(line: String): Option[Message] = {
      val mt: Option[MessageType] = messageTypes.find(mt => line.startsWith(mt.name))
      mt match {
	case None => None
	case Some(messageType) =>
	  val startIndex = messageType.name.length+1
	  val content = line.drop(startIndex)
	  Some(new Message(content = content, messageType = messageType))
      }
    }
  }
  class Message(
    val content: String,
    val messageType: MessageType) {
  }

  def createDirectory(parentDirectory: Path, commandText: String,
		      description: String): Path = {
    val directory = Task.createDirectory(parentDirectory, description)
    // Generate the TCL script that this Vivado process will run.
    val tclDirectory = getClass.getResource("/tcl/").getPath
    val commandPath = directory.resolve("command.tcl")
    val templatePath = Paths.get(getClass.getResource("/tcl/vivado_task.tcl.t").getPath)
    val templateArgs = Map(
      "command" -> commandText,
      "tclDirectory" -> tclDirectory)
    debug(s"Creating a new VivadoTask in directory $directory")
    debug(s"Command is $commandText")
    TemplateHelper.formatTemplate(
      values = templateArgs,
      templatePath = templatePath,
      outputPath = commandPath)
    directory
  }

  def create(parentDirectory: Path, commandText: String,
	     description: String): VivadoTask = {
    val directory = createDirectory(parentDirectory, commandText, description)
    new VivadoTask(directory)
  }
}

class VivadoTask(directory: Path)
extends Task(directory = directory) {
        
  def run() = {
    val p = sys.process.Process(
      command = Seq(Config.vivado.toString, "-mode", "batch",
		    "-log", "stdout.txt", "-source", "command.tcl"),
      cwd = directory.toFile)
    p.lines
  }

  /** Get any messages that the vivado process wrote to it's output.
    * and work out what type of message they were (e.g. ERROR, INFO...).
        
    * @param ignoreStrings Is a list of strings which when present in
    *           Vivado messages we ignore.
    */
  def getMessages(ignoreStrings: Seq[String] = Config.defaultIgnoreStrings):
  Iterator[VivadoTask.Message] = {
    val lines = Source.fromFile(directory.resolve("stdout.txt").toFile).getLines()
    val allMessages = lines.map(VivadoTask.Message.fromLine).flatten
    allMessages.filter(m => !ignoreStrings.exists(m.content.contains(_)))
  }

  def getErrorMessages(ignoreStrings: Seq[String] = Config.defaultIgnoreStrings):
  Iterator[VivadoTask.Message] = {
    getMessages(ignoreStrings).filter(m => VivadoTask.errorMessageTypes.contains(m.messageType))
  }

  def waitAndLog(pollInterval: Long = 1000) = {
    waitUntilFinished(pollInterval)
    getMessages().foreach(m =>
      m.messageType.logFunction(m.content)
    )
  }

}