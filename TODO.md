## TODO

- Fix bug: size of AboutDialog is broken on vertical monitors
- Show version in AboutDialog
- README: add screenshot
- Maybe cache resource files
    > I don't know if there are any guarantees by Java runtime that a resource is cached or not.  We might want to just cache a byte array, and convert it into a ByteInputStream for playing the sound.
    * [ ] Check how TOS play sounds
    * [ ] Find more about resource loading
- Java modules
    * [ ] https://stackoverflow.com/a/62959016/1083697
    * [ ] Is GSON library modularized?
- different button layout - user setting. ButtonLayout should be a "setting" of YearPanel.  Styles: columns, rows, calendar
- Habit description
    * [x] depends on the JSON rewrite
    * [ ] add field for habit description
- Introduce Help > Help
- Remember last opened tab
    * [ ] Refactor passing around of `Path dataDir` -- just store `ProjectDirectories` instance in a field in `MainGui`.   This way, the list of `somethingDir` parameters won't be weirdly growing. (we need `cacheDir` next)
    * [ ] Save last opened tab in cache
- Checkout Chatty's source code
    > Chatty seems to be doing a lot of nice things with Java 8, desktop integration-wise.
    * [ ] browser opening
    * [x] directory for saving (~/.chatty)
    * [ ] something else?
    * [x] icon for JAR file -- lol what? that's not possible. OS/DE decides the icon. Doable only for an .exe file
- Improve .habit format
    * [ ] Compress size somehow
- new button style: override JComponent#paint & addMouseListener instead of boring buttons
- Streak sound
- Streak animation 
- Starter interface (suggest three habits)
    * [ ] or just show contents of "Add habit" dialog in the main window instead of tabs
- When adding more than (some small number) of habits, warn the user
- Save button
    > Just a kind of "peace of mind" thing. Although auto-saving is much more intuitive, because save location is pre-determined. Users don't quote-unquote "edit" their .habit files, they are turning lights (toggle buttons) on and off
    * [ ] Add save button
    * [x] Disable after autosave/Enable after edit --- automatic via YearHistory#hasChanges
- Add shortcut for opening the habit directory via Desktop.open
    * [ ] Or some kind of "Help > Debug > Open data dir" menu
- Add intro panel when zero habit files exist
    > Intro panel may use the same UI as the "Add habit" dialog.
    * [ ] use CardLayout to swap between JTabbedPane and intro panel
- gitignore
- https://openjdk.java.net/jeps/392 ?

## In dev

- Introduce Help > License
    * [x] Extract Url utils from AboutDialog
    * [x] Add licensing information -- this is probably even required for GPL "compliancy"
    * [ ] Add attribution for used libraries

## Done

- Idea from Veritasium video about New Year resolutions https://www.youtube.com/watch?v=Pm9CQn07OjU
- bold font on today button
- Automatic periodic saving
- Move year -- ++ buttons
- Save via temp files
- application plugin: make app name independent of project.name
- jar manifest main class for click launch in most OSs
- Choose app name (see README)
- Shortcut: mark current day in current tab
- Folder? Yes!
- Multiple files
- Any year pick
- Prototype
- Help > About dialog
- Fix ALSA leaking in KDE volume settings
    > (In KDE)     S2R: 1) play a bunch of sounds 2) look at the volume mixer "Applications" tab. Expected results: all Resoday entries disappear. Actual result: number of Resoday entries increases.
- Remove .habit suffix from GUI
    > Tab names in the GUI don't need the .habit suffix. It only takes up horizontal space and is not useful
- Saving/reading data to/from $XDG_DATA_HOME
    * [x] read XDG base directory spec
    * [x] check what defaulting to $HOME/.local/... means when $HOME is null/empty
- Manually test the changes from 2021-04-29
- Double check how audio resources are closed
- Refactor getting current tab
    > Probably makes sense to extract it into a private method
- See stashes
    * [x] unit tests for sanitization---during storage rewrite, sanitization is only needed for control characters (including Unicode ones, preferrably)
- Rewrite disk storage
    > Current approach with filenames has problems.  Filenames should be programatically generated to avoid issues from user input.  User input, on the other hand, shouldn't be restricted without a reason.
    * [x] use UUIDs for filenames
    * [x] store habit name in the file as a field
    * [x] use JSON?
    * [x] store version of Resoday in the file as a field
    * [x] don't forget to migrate your own resoday files in the process
- UI to create habit files
    * [x] Make a functional (FP) API for the dialog---implementation in stash has bad API
- App icon
    * [x] draw icon
    * [x] green code
    * [x] add icon
    * [x] test
- File format versioning
    > YES! Just saving the file format version in the file allows to migrate in the future more easily.
    * [x] extract file reading to separate package
    * [x] develop v1 of the file format
- Rename menu "Habits" to "Main"
    > see Chatty's UI
- Should we sort the tabs by filename? Allows for simple 01 02 03 filename prefix ordering
    * [x] better to allow reordering
- UI to rename habits
- Remember window size and location
    > Like Bash saves history in .bash_history, Resoday should save UI state (window size/position, selected tab, etc) in XDG_CACHE_HOME/resoday/ui_state.   See https://unix.stackexchange.com/a/631147/53143 for details
    * [x] Treat window size as an implicitly set setting---user controls it, just not through a dialog with comboboxes and checkboxes.
- Do we need reordering of tabs/files?
- Config in $XDG_CONFIG_HOME
    > Still not clear what exactly should be saved in the config.  There are no changeable settings at the moment
    * [x] trivial via `directories.jar`
    * [x] used for window position
- Use xdg-open for URLs
- 2021-11 Clean up TODO
- UI to hide/show habit files
    > Introduce a new field in the file. UI is a list of all files which are either shown or hidden, with "show/hide" button alongside.
    * [x] depends on the JSON rewrite
    * [x] Think how HabitHolder class is going to work
    * [x] "hidden" should be a property saved in the files
    * [x] if "hide" behavior is kept, then "Add habit" dialog would need to keep track of names of hidden habits as well to avoid collisions --- this would require something like class HabitsStorage
    * [x] implement class Histories
    * [x] add `.local/share/resoday/habit-order.res` to preserve order between restarts
    * [x] not implemented -- maybe add test for non-empty v0 file -- not implemented
    * [x] make Edit dialog more user-friendly: needs buttons: [Ok] [Cancel] [Apply] --- implemented without the "Apply" button for now.
- Fix icon in the dialogs
    > all modal dialogs have the default Duke icon, which is unfortunate
- UI to delete habits
    > An irreversible deletion of habits should be possible
- Simplify build instructions
    > Or better yet -- create a release on GitHub and GitLab with fat jar attached.
    * [x] check if any libraries are used already
    * [x] if habit file structure is overhauled (e.g. JSON all the things) then a fat jar will be needed to include the JSON library at least.
    * [x] Gradle task to generate fat jar from(configurations.implementation)
- Commit README, LICENSE, etc
    * [x] depends on Gradle rewrite for jar file building
    * [x] Write README
    * [x] Choose license (probably GPL3)
    * [x] Commit files
    * [x] Code style
- Add Git* links to AboutDialog
    > Right now Help>About is only about the YouTube video.    It should contain more useful information. See also other tasks in "TODO".
    * [x] Add link to GitLab/GitHub/etc
