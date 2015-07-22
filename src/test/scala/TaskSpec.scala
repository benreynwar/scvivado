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
     intercept[java.util.NoSuchElementException] {
       Task.TaskStates.withName("BAD_NAME")
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

   "A TemplateHelper" should "format strings" in {
     val template = "Test this @one@ and @two@"
     val args = Map("one" -> "1",
   		   "two" -> "2")
     val formatted = TemplateHelper.formatString(args, template)
     assert(formatted == "Test this 1 and 2")
   }

  "A VivadoTask" should "be able to be created" in withTasksDirectory {
    tasksDirectory => {
      val description = "Something with Vivado"
      val t = VivadoTask.create(
        parentDirectory = tasksDirectory,
        description = description,
        commandText = "do_this_super_thing now")
      t.run()
      t.waitUntilFinished(1000)
      val msgs: List[VivadoTask.Message] = t.getErrorMessages().toList
      assert(msgs.length == 1)
      assert(msgs(0).content.contains("do_this_super_thing"))
    }
  }

}
