# -*- tcl -*- 

# Update the state of this task to 'RUNNING'.
set state_f state.txt
set fileId [open $state_f "w"]
puts -nonewline $fileId RUNNING
close $fileId
# Put our command in a catch so that if we have errors in
# the command, we'll still update the state correctly before
# exiting.
if {[catch {
  lappend auto_path {@tclDirectory@}
  package require pyvivado
  # And the actual command that this task was created to perform.
  @command@
} message]} {
  # Handle an error in the command.
  puts "ERROR: $message"
  set fileId [open $state_f "w"]
  puts -nonewline $fileId FINISHED_ERROR
  close $fileId
}
# Everything went smoothly so update our state
# with FINISHED_OK.
set fileId [open $state_f "w"]
puts -nonewline $fileId FINISHED_OK
close $fileId
