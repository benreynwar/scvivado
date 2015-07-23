import java.io.File
import java.nio.file.Paths
import org.scalatest._
import org.apache.commons.io.FileUtils

class ProjectSpec extends FlatSpec {
  "A project" should "be able to be created." in {
    val projectDirectory = Paths.get("qa_project_directory")
    projectDirectory.toFile.mkdir()
    try {
      val dummyFile: File = new File(getClass.getResource("/hdl/dummy.vhd").getFile)
      val designFiles = Set(dummyFile)
      val simulationFiles = Set[File]()
      val (p, t) = Project.create(
        directory = projectDirectory,
        designFiles = designFiles,
        simulationFiles = simulationFiles
      )
      t.waitAndLog()
      assert(t.getState == Task.TaskStates.FinishedOK)
    } finally {
      FileUtils.deleteDirectory(projectDirectory.toFile)
    }
  }
}