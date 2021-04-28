## TODO

- File format versioning
    > YES! Just saving the file format version in the file allows to migrate in the future more easily.
    * [ ] extract file reading to separate package
    * [ ] develop v1 of the file format
- Rename menu "Habits" to "Main"
    > see Chatty's UI
- Simplify build instructions
    > Since there are no dependencies, distZip is not needed---just a jar file is OK.
    * [ ] check if any libraries are used already
    * [ ] if habit file structure is overhauled (e.g. JSON all the things) then a fat jar will be needed to include the JSON library at least.
- Commit README, LICENSE, etc
    * [ ] depends on Gradle rewrite for jar file building
    * [ ] Write README
    * [x] Choose license (probably GPL3)
    * [x] Commit files
    * [x] Code style
- Checkout Chatty's source code
    > Chatty seems to be doing a lot of nice things with Java 8, desktop integration-wise.
    * [ ] browser opening
    * [ ] directory for saving (~/.chatty)
    * [ ] something else?
    * [ ] icon for JAR file
- Improve Help > About
    > Right now Help>About is only about the YouTube video.    It should contain more useful information.
    * [ ] Add link to GitLab/GitHub/etc
    * [ ] Add licensing information -- this is probably even required for GPL "compliancy"
    * [ ] Add version info from baked jar MANIFEST
    * [ ] Add attribution for used libraries
- UI to hide/show habit files
    > Introduce a new field in the file. UI is a list of all files which are either shown or hidden, with "show/hide" button alongside.
    * [ ] depends on the JSON rewrite
- Maybe cache resource files
    > I don't know if there are any guarantees by Java runtime that a resource is cached or not.  We might want to just cache a byte array, and convert it into a ByteInputStream for playing the sound.
    * [ ] Check how TOS play sounds
    * [ ] Find more about resource loading
- Remember window size and location
    > Like Bash saves history in .bash_history, Resoday should save UI state (window size/position, selected tab, etc) in XDG_CACHE_HOME/resoday/ui_state.   See https://unix.stackexchange.com/a/631147/53143 for details
    * [ ] Treat window size as an implicitly set setting---user controls it, just not through a dialog with comboboxes and checkboxes.
- Java modules
    * [ ] https://stackoverflow.com/a/62959016/1083697
    * [ ] Is GSON library modularized?
- Habit description
    * [ ] depends on the JSON rewrite
    * [ ] add field for habit description
- Improve .habit format
    * [ ] Idea: <name>/YYYY.habit
    * [ ] Compress size somehow
    * [ ] Add magic tag to the plain text format
- Should we sort the tabs by filename? Allows for simple 01 02 03 filename prefix ordering
- Do we need reordering of tabs/files?
- different button layout - user setting. ButtonLayout should be a "setting" of YearPanel.  Styles: columns, rows, calendar
- new button style: override JComponent#paint & addMouseListener instead of boring buttons
- Streak sound
- Streak animation 
- Starter interface (suggest three habits)
- When adding more than (some small number) of habits, warn the user
- Config in $XDG_CONFIG_HOME
    > Still not clear what exactly should be saved in the config.  There are no changeable settings at the moment
- Save button
    > Just a kind of "peace of mind" thing. Although auto-saving is much more intuitive, because save location is pre-determined. Users don't quote-unquote "edit" their .habit files, they are turning lights (toggle buttons) on and off
    * [ ] Add save button
    * [ ] Disable after autosave/Enable after edit

## In dev

- UI to delete habit files
    > Menu item which deletes the habit file corresponding to the  currently opened tab.
    * [ ] disable menu item when nothing to delete
- Rewrite disk storage
    > Current approach with filenames has problems.  Filenames should be programatically generated to avoid issues from user input.  User input, on the other hand, shouldn't be restricted without a reason.
    * [ ] use JSON?
    * [ ] use UUIDs for filenames
    * [ ] store habit name in the file as a field
    * [ ] store version of Resoday in the file as a field
    * [ ] don't forget to migrate your own resoday files in the process

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
- UI to create habit files
