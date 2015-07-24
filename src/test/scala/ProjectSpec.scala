import java.nio.file.{Paths, Path}
import org.scalatest._
import org.apache.commons.io.FileUtils

class ProjectSpec extends FlatSpec {
  "A project" should "be able to be created and synthesized." in {
    val projectDirectory = Paths.get("qa_project_directory")
    projectDirectory.toFile.mkdir()
    try {
      val dummyFile: Path = Paths.get(getClass.getResource("/hdl/dummy.vhd").getPath)
      val designFiles = Set(dummyFile)
      val simulationFiles = Set[Path]()
      val (p, t) = Project.create(
        directory = projectDirectory,
        designFiles = designFiles,
        simulationFiles = simulationFiles
      )
      t.waitAndLog()
      assert(t.getState == Task.TaskStates.FinishedOK)
      val synthTask = p.synthesize()
      synthTask.waitAndLog()
      assert(synthTask.finishedWithNoErrors)
    } finally {
      FileUtils.deleteDirectory(projectDirectory.toFile)
    }
  }
}