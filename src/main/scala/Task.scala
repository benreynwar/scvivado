import java.nio.file.{Path, Paths, Files}
import java.io._
import scala.io.Source._

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
  val lastIdFilename = "lastId.txt"

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
  def getNextId(directory: Path) = {
    val p = directory.resolve(lastIdFilename)
    val f = p.toFile
    val nextId = if (Files.exists(p)) {
      val lines = fromFile(f).getLines
      lines.next.toInt + 1
    } else {0}
    val pw = new PrintWriter(f)
    try pw.write(nextId.toString) finally pw.close()
    nextId
  }

  
  /**
    * Create a new task.  Mostly just setting the database entry up,
    * creating the directory and stuff like that.  Subclasses of this
    *    do the real work.
    *    
    * @param parentDirectory The directory in which we place create the
    *            tasks directory.
    * @param description Describes the task.
    * @param id
    */
  def create(parentDirectory: Path, description: String = ""): Task = {
    if (!Files.exists(parentDirectory)) {
      throw new Exception(s"Parent directory of task $parentDirectory does not exist")
    }
    val id = getNextId(parentDirectory)
    val dn = s"task_${id}"
    val directory = parentDirectory.resolve(dn)
    directory.toFile().mkdir()
    setStateHelper(directory, TaskStates.NotStarted)
    setDescriptionHelper(directory, description)
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
    val lines = fromFile(statePath.toFile).getLines
    Task.TaskStates.withName(lines.next)
  }

  def setDescription(description: String) = {
    Task.setDescriptionHelper(directory, description)
  }

  def getDescription: String = {
    fromFile(descriptionPath.toFile).getLines.mkString
  }

  if (!Files.exists(directory)) {
    throw new Exception("Task's directory does not exist: $directory")
  }
  val description = getDescription
}
