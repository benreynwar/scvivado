import org.scalatest._
import java.nio.file.{Path, Paths, Files}
import org.apache.commons.io.FileUtils


class TaskSpec extends FlatSpec with Matchers {

  def withTasksDirectory(testCode: Path => Any) {
    val tasksDirectory = Paths.get("temp_tasks_directory")
    tasksDirectory.toFile.mkdir()
    try {
      testCode(tasksDirectory)
    } finally {
      FileUtils.deleteDirectory(tasksDirectory.toFile)
    }
  }

  "A TaskState" should "be able to be created" in {
    val ts1 = Task.TaskStates.NotStarted
    val ts2 = Task.TaskStates.withName("NOT_STARTED")
    assert(ts1 == ts2)
    val thrown = intercept[java.util.NoSuchElementException] {
      val ts3 = Task.TaskStates.withName("BAD_NAME")
    }
  }

  "A Task" should "be able to be created" in withTasksDirectory {
    tasksDirectory =>
      val description = "Blah blah"
      val t = Task.create(
      parentDirectory = tasksDirectory,
      description = description)
      assert(t.description == description)
      assert(t.directory == tasksDirectory.resolve("task_0"))
  }  

}
