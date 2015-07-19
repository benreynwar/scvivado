import java.nio.file.{Path, Paths, Files}

/**    
  *  An external process that we run.
  * 
  *  Each task has it's own directory created for it.
  *  This directory contains the following files:
  *   - current_state.txt - either NOT_STARTED, RUNNING, FINISHED_OK, FINISHED_ERROR.
  *   - final_state.txt either FINISHED_OK or FINISHED_ERROR
  */
object Task {

  object TaskStates extends Enumeration {
    val NotStarted = Value("NOT_STARTED")
    val Running = Value("RUNNING")
    val FinishedOK = Value("FINISHED_OK")
    val FinishedError = Value("FINISHED_ERROR")
  }
  type TaskState = TaskStates.Value

  class RawTaskInfo(val parentDirectory: Path, val description: String,
		    val state: TaskState) {
  }
  class TaskInfo(
    parentDirectory: Path, description: String,
    state: TaskState, val id: Int)
  extends RawTaskInfo(parentDirectory = parentDirectory,
		      description = description,
		      state = state) {
  }
  
  trait TasksCollection {
    def insert(info: RawTaskInfo): TaskInfo
  }

  class DummyTasksCollection extends TasksCollection{
    def insert(info: RawTaskInfo): TaskInfo = {
      new TaskInfo(
	parentDirectory = info.parentDirectory,
	description = info.description,
	state = info.state,
	id = 0
      )
    }
  }

  /**
    * Create a new task.  Mostly just setting the database entry up,
    * creating the directory and stuff like that.  Subclasses of this
    *    do the real work.
    *    
    * @param parentDirectory The directory in which we place create the
    *            tasks directory.
    * @param tasksCollection How we keep track of tasks.
    * @param description Describes the task.
    * @param id
    */
  def create(parentDirectory: Path, tasksCollection: TasksCollection,
	     description: String = ""): Task = {
    if (!Files.exists(parentDirectory)) {
      throw new Exception(s"Parent directory of task $parentDirectory does not exist")
    }
    val record = new RawTaskInfo(
      parentDirectory = parentDirectory,
      description = description,
      state = TaskStates.NotStarted
    )
    val updatedRecord = tasksCollection.insert(record)
    val dn = s"task_${updatedRecord.id}"
    val directory = parentDirectory.resolve(dn)
    directory.toFile().mkdir()
    val t = new Task(id = updatedRecord.id, tasksCollection = tasksCollection)
    //t.setCurrentState(NotStarted)
    t
  }
}

class Task(val id: Int, val tasksCollection: Task.TasksCollection) {
}
        
    // def currentStateFn(self):
    //     fn = os.path.join(self.directory, 'current_state.txt')
    //     return fn

    // def set_current_state(self, state):
    //     '''
    //     Sets the state in the state file.
    //     '''
    //     fn = self.current_state_fn()
    //     if state not in self.POSSIBLE_STATES:
    //         raise ValueError('State of {} is unknown.'.format(state))
    //     with open(fn, 'w') as f:
    //         f.write(state)
        

    // def get_current_state(self):
    //     '''
    //     Get the current state of this task.
    //     '''
    //     fn = self.current_state_fn()
    //     with open(fn, 'r') as f:
    //         state = f.read().strip()
    //     if state not in self.POSSIBLE_STATES:
    //         raise ValueError('State of {} is unknown.'.format(state))
    //     return state

    // def __init__(self, _id, tasks_collection):
    //     '''
    //     Get the task corresponding to the passed id.
    //     '''
    //     self.record = tasks_collection.find_by_id(_id)
    //     self._id = str(self.record['id'])
    //     self.parent_directory = self.record['parent_directory']
    //     self.description = self.record.get('description', '')
    //     if not os.path.exists(self.parent_directory):
    //         raise Exception(
    //             'Cannot find tasks parent directory {}'
    //             .format(self.parent_directory))
    //     dn = 'task_' + self._id
    //     self.directory = os.path.join(self.parent_directory, dn)
    //     if not os.path.exists(self.directory):
    //         raise Exception(
    //             'Cannot find tasks directory {}'
    //             .format(self.directory))
        
