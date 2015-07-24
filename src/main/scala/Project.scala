import java.nio.file.{Path, Files}
import grizzled.slf4j.Logging

class Part {}
class Board {}
class IP(val generatorName: String, val parameters: Map[String, String],
         val moduleName: String) {
  def getTCL = {
    val version = ""
    val tclProps = parameters.map({case (k, v) => s"{$k $v}"}).mkString(" ")
    s"{ $generatorName $version $moduleName { $tclProps } }"
  }
}

/**
  * The base class for python wrappers around Vivado Projects.
  *
  *  Also does some management of Vivado processes (`Task`s) that are run.
  */
object Project extends Logging{

  case class AlreadyExistsException(message: String) extends Exception(message)

  /**
   * Create a new Vivado project.
   *
   * @param directory The directory where the project will be created.
   * @param designFiles The files for the synthesizable fraction of the design.
   * @param simulationFiles The simulation files (i.e. non-synthesizable).
   * @param part The 'part' to use when implementing.
   * @param board The 'board' to used when implementing.
   * @param ips A list of the ip blocks with their names and parameters used.
   * @param topModuleName The name of the top level module.
   */
  def setUp(directory: Path, designFiles: Set[Path],
            simulationFiles: Set[Path], part: Option[Part] = None,
            board: Option[Board] = None, ips: Set[IP] = Set(),
            topModuleName: String = ""): VivadoTask = {
    if (Files.exists(directory.resolve("TheProject.xpr"))) {
      throw AlreadyExistsException("Project already exists.")
    }
    val tclIPs = ips.map(_.getTCL).mkString(" ")
    val designFilesString = designFiles.map(
      f => s"{${f.toString}}").mkString(" ")
    val simulationFilesString = simulationFiles.map(
      f => s"{${f.toString}}").mkString(" ")
    val partString = part match {
      case Some(p) => p.toString
      case None => ""
    }
    val boardString = board match {
      case Some(b) => b.toString
      case None => ""
    }
    val command = s"::pyvivado::create_vivado_project {${directory.toAbsolutePath}} { $designFilesString } { $simulationFilesString } {$partString} {$boardString} {$tclIPs} {$topModuleName}"
    debug(s"Command is $command")
    debug(s"Directory of new project is $directory")
    val t = VivadoTask.create(parentDirectory = directory, commandText = command,
      description = "Creating a new Vivado project.")
    t.run()
    t
  }

  def create(directory: Path, designFiles: Set[Path],
             simulationFiles: Set[Path], part: Option[Part] = None,
             board: Option[Board] = None, ips: Set[IP] = Set(),
             topModuleName: String = ""): (Project, VivadoTask) = {
    val t: VivadoTask = setUp(
      directory = directory,
      designFiles = designFiles,
      simulationFiles = simulationFiles,
      part = part,
      board = board,
      ips = ips,
      topModuleName = topModuleName
    )
    (new Project(directory), t)
  }
}

/**
  *Create a python wrapper around a Vivado project.
  *
  * @param directory Location of the vivado project.
  */
class Project(directory: Path) {
  val filename = directory.resolve("TheProject.xpr")

  /**
   * Get all the tasks (Vivado processes) that have been run on this proejct.
   * We get them from their directories in the project directory
   */
  def getTasks: Iterable[Task] = {
    new TaskCollection(directory).getTasks
  }

  def getUnfinishedTasks = getTasks.filter(x => !x.isFinished)

  /**
   * Spawn a Vivado process to synthesize the project.
   */
  def synthesize(keepHierarchy: Boolean = false): VivadoTask = {
    val commandStart = s"::pyvivado::open_and_synthesize {${directory.toAbsolutePath}}"
    val commandText = if (keepHierarchy)
      s"$commandStart keep_hierarchy"
    else
      s"$commandStart {}"
    val t = VivadoTask.create(
      parentDirectory = directory,
      commandText = commandText,
      description = "Synthesize project."
    )
    t.run()
    t
  }

  /**
   * Spawn a Vivado process to implement the project.
   */
  def implement(): VivadoTask = {
    val commandText = s"::pyvivado::open_and_implement ${directory.toAbsolutePath}"
    val t = VivadoTask.create(
      parentDirectory = directory,
      commandText = commandText,
      description = "Implement project.")
    t.run()
    t
  }
}
