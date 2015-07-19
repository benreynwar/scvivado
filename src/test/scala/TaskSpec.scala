import org.scalatest._
import java.nio.file.{Path, Paths, Files}
import org.apache.commons.io.FileUtils


class TaskSpec extends FlatSpec with Matchers {

  "A TaskState" should "be able to be created" in {
    val ts1 = Task.TaskStates.NotStarted
    val ts2 = Task.TaskStates.withName("NOT_STARTED")
    assert(ts1 == ts2)
    val thrown = intercept[java.util.NoSuchElementException] {
      val ts3 = Task.TaskStates.withName("BAD_NAME")
    }
  }

  "A DummaryTasksCollection" should "be able to be created" in {
    val tc = new Task.DummyTasksCollection
  }

  val tasksDirectory = Paths.get("temp_tasks_directory")
  tasksDirectory.toFile().mkdir()

  "A Task" should "be able to be created" in {
    val tc = new Task.DummyTasksCollection
    val t = Task.create(
      parentDirectory = tasksDirectory,
      tasksCollection = tc,
      description = "Testing task")
  }
  
  FileUtils.deleteDirectory(tasksDirectory)

}
