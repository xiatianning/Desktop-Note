import javafx.application.Application
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlin.properties.Delegates

class Main : Application()  {
    override fun start(stage: Stage) {
        // Root Node
        val root = BorderPane()

        // Title
        stage.title = "CS349 - A1 Notes - t37xia"

        // Toolbar - “List” and “Grid” buttons
        val listButton = Button("List").apply {
            prefHeight = 25.0
            prefWidth = 50.0
            setDisable(true)
        }
        val gridButton = Button("Grid").apply {
            prefHeight = 25.0
            prefWidth = 50.0
            requestFocus()
        }
        var showList: Boolean by Delegates.observable(true) { _, old, new ->
            if (old != new) {
                listButton.setDisable(new)
                gridButton.setDisable(!new)
                if (new) {
                    gridButton.requestFocus()
                } else {
                    listButton.requestFocus()
                }
            }
        }

        // Toolbar - “Show archived” checkbox
        var showArchived = false
        val showCheckbox = CheckBox()

        // Toolbar - “Order by” choicebox
        var order = "Length (asc)"
        val orderList = FXCollections.observableArrayList(
            "Length (asc)",
            "Length (desc)",
            "Alphabetical (asc)",
            "Alphabetical (desc)",
            "Starred First"
        )
        val orderChoice = ChoiceBox(orderList).apply {
            selectionModel.select(0)
        }

        // Toolbar - "clear" button
        val clearButton = Button("Clear").apply {
            prefHeight = 25.0
            prefWidth = 50.0
        }

        // Toolbar
        val toolbar = ToolBar(
            Label("View:").apply { padding = Insets(10.0) },
            HBox(listButton, gridButton).apply {
                spacing = 10.0
                padding = Insets(10.0, 10.0, 10.0, 0.0)
            },
            Separator(),
            Label("Show archived:").apply { padding = Insets(10.0) },
            showCheckbox.apply { padding = Insets(10.0, 10.0, 10.0, 0.0) },
            Separator(),
            Label("Order by:").apply { padding = Insets(10.0) },
            orderChoice,
            Pane().apply { HBox.setHgrow(this, Priority.ALWAYS) },
            HBox(clearButton).apply { padding = Insets(10.0, 10.0, 10.0, 10.0) },
        )
        toolbar.style = "-fx-padding: 0px;"  // Eliminate the default padding of ToolBar

        // Data class for Note
        data class Note(var content: String, var isArchived: Boolean, var isStarred: Boolean)

        // Initial notes for display
        var notes = mutableListOf(
            Note("This a simple note taking application. A note has a text body and it can be marked as archived. " +
                    "They are displayed in a list or in a grid. You can filter to show only non-archived notes" +
                    "and you can sort notes according to their length in ascending or descending order. " +
                    "You can create new notes and clear all existing ones.", false, true),
            Note("A Short Note.", true, false),
            Note("Have a nice day!!", false, true),
            Note("This is \n a multi-line \n note \n \n :)", true, false),
            Note("This is an active note", false, false),
            Note("One.", false, false),
            Note("Two.", false, false),
            Note("Three.", false, false),
        )

        // Function for sorting notes based on "Order by" option
        fun sortNotes() {
            if (order == "Length (asc)") {
                notes = notes.sortedWith <Note> (object : Comparator <Note> {
                    override fun compare (n0: Note, n1: Note) : Int {
                        if (n0.content.length > n1.content.length) {
                            return 1
                        }
                        if (n0.content.length == n1.content.length) {
                            return 0
                        }
                        return -1
                    }
                }).toMutableList()
            } else if (order == "Length (desc)") {
                notes = notes.sortedWith <Note> (object : Comparator <Note> {
                    override fun compare (n0: Note, n1: Note) : Int {
                        if (n0.content.length < n1.content.length) {
                            return 1
                        }
                        if (n0.content.length == n1.content.length) {
                            return 0
                        }
                        return -1
                    }
                }).toMutableList()
            } else if (order == "Alphabetical (asc)") {
                notes = notes.sortedBy { it.content }.toMutableList()
            } else if (order == "Alphabetical (desc)") {
                notes = notes.sortedBy { it.content }.reversed().toMutableList()
            } else if (order == "Starred First") {
                notes = notes.sortedWith <Note> (object : Comparator <Note> {
                    override fun compare (n0: Note, n1: Note) : Int {
                        if (!n0.isStarred && n1.isStarred) {
                            return 1
                        }
                        if ((n0.isStarred && n1.isStarred) || (!n0.isStarred && !n1.isStarred)) {
                            return 0
                        }
                        return -1
                    }
                }).toMutableList()
            }
        }

        // Status Bar
        fun statusbar() : HBox {
            val total = notes.size
            val active = notes.fold(0) { acc, elem ->
                if (!elem.isArchived) {
                    acc + 1
                } else {
                    acc
                }
            }
            val star = notes.fold(0) { acc, elem ->
                if (elem.isStarred) {
                    acc + 1
                } else {
                    acc
                }
            }
            return HBox(
                Label("$total note${if (total == 1) "" else "s"}, " +
                        "$active of which ${if (active == 1) "is" else "are"} active, " +
                        "$star of which ${if (star == 1) "is" else "are"} starred"
                ).apply {
                    padding = Insets(2.0)
                }
            )
        }

        // List view of notes
        fun listview() : ScrollPane {
            sortNotes()
            // List view special note
            val textarea = TextArea().apply {
                prefHeight = 75.0
                HBox.setHgrow(this, Priority.ALWAYS)
            }
            val listSpecialNote = HBox(
                textarea,
                Button("Create").apply {
                    prefWidth = 75.0
                    prefHeight = 42.0
                    onAction = EventHandler {
                        notes.add(Note(textarea.text, false, false))
                        root.center = listview()
                        root.bottom = statusbar()
                    }
                }
            ).apply {
                spacing = 10.0
                prefHeight = 62.0
                padding = Insets(10.0);
                background = Background(
                    BackgroundFill(
                        Color.LIGHTSALMON,
                        CornerRadii(10.0),
                        Insets.EMPTY)
                )
            }
            // Return list view content
            return ScrollPane(
                VBox(listSpecialNote).apply {
                    spacing = 10.0
                    padding = Insets(10.0)
                    for (i in notes.indices) {
                        val note = notes[i]
                        if (!note.isArchived || showArchived) {
                            children.add(
                                HBox(
                                    Label(note.content).apply { isWrapText = true },
                                    Pane().apply { HBox.setHgrow(this, Priority.ALWAYS) },
                                    CheckBox().apply {
                                        isSelected = note.isStarred
                                        selectedProperty().addListener { _, _, newValue ->
                                            notes[i].isStarred = newValue
                                            root.center = listview()
                                            root.bottom = statusbar()
                                        }
                                    },
                                    Label("Starred").apply {
                                        minWidth = 55.0
                                        HBox.setHgrow(this, Priority.NEVER)
                                    },
                                    CheckBox().apply {
                                        isSelected = note.isArchived
                                        selectedProperty().addListener { _, _, newValue ->
                                            notes[i].isArchived = newValue
                                            root.center = listview()
                                            root.bottom = statusbar()
                                        }
                                    },
                                    Label("Archived").apply {
                                        minWidth = 55.0
                                        HBox.setHgrow(this, Priority.NEVER)
                                    },
                                    Button("Delete").apply {
                                        minWidth = 75.0
                                        HBox.setHgrow(this, Priority.NEVER)
                                        onAction = EventHandler {
                                            notes.removeAt(i)
                                            root.center = listview()
                                            root.bottom = statusbar()
                                        }
                                    }
                                ).apply {
                                    spacing = 10.0
                                    padding = Insets(10.0)
                                    background = Background(
                                        BackgroundFill(
                                            if (note.isArchived) {
                                                Color.LIGHTGRAY
                                            } else if (note.isStarred) {
                                                Color.LIGHTPINK
                                            } else {
                                                Color.LIGHTYELLOW
                                            },
                                            CornerRadii(10.0),
                                            Insets.EMPTY
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            ).apply {
                setFitToWidth(true)
                hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            }
        }

        // Grid view of notes
        fun gridview() : ScrollPane {
            sortNotes()
            // Grid view special note
            val textarea = TextArea().apply {
                prefWidth = 225.0
                VBox.setVgrow(this, Priority.ALWAYS)
            }
            val gridSpecialNote = VBox(
                textarea,
                Button("Create").apply {
                    prefWidth = 225.0
                    prefHeight = 25.0
                    onAction = EventHandler {
                        notes.add(Note(textarea.text, false, false))
                        root.center = gridview()
                        root.bottom = statusbar()
                    }
                }
            ).apply {
                spacing = 10.0
                prefWidth = 225.0
                prefHeight = 225.0
                padding = Insets(10.0);
                background = Background(
                    BackgroundFill(
                        Color.LIGHTSALMON,
                        CornerRadii(10.0),
                        Insets.EMPTY)
                )
            }
            // Return grid view content
            return ScrollPane(
                FlowPane(gridSpecialNote).apply {
                    hgap = 10.0
                    vgap = 10.0
                    padding = Insets(10.0)
                    for (i in notes.indices) {
                        val note = notes[i]
                        if (!note.isArchived || showArchived) {
                            children.add(
                                VBox(
                                    Label(note.content).apply { isWrapText = true },
                                    Pane().apply { VBox.setVgrow(this, Priority.ALWAYS) },
                                    HBox(
                                        CheckBox().apply {
                                            isSelected = note.isStarred
                                            selectedProperty().addListener { _, _, newValue ->
                                                notes[i].isStarred = newValue
                                                root.center = listview()
                                                root.bottom = statusbar()
                                            }
                                        },
                                        Label("Starred").apply {
                                            minWidth = 55.0
                                            HBox.setHgrow(this, Priority.NEVER)
                                        },
                                        CheckBox().apply {
                                            isSelected = note.isArchived
                                            selectedProperty().addListener { _, _, newValue ->
                                                notes[i].isArchived = newValue
                                                root.center = gridview()
                                                root.bottom = statusbar()
                                            }
                                        },
                                        Label("Archived")
                                    ).apply { spacing = 10.0 },
                                    Button("Delete").apply {
                                        prefWidth = 225.0
                                        prefHeight = 25.0
                                        onAction = EventHandler {
                                            notes.removeAt(i)
                                            root.center = gridview()
                                            root.bottom = statusbar()
                                        }
                                    }
                                ).apply {
                                    spacing = 10.0
                                    padding = Insets(10.0)
                                    prefWidth = 225.0
                                    prefHeight = 225.0
                                    HBox.setHgrow(this, Priority.NEVER)
                                    VBox.setVgrow(this, Priority.NEVER)
                                    background = Background(
                                        BackgroundFill(
                                            if (note.isArchived) {
                                                Color.LIGHTGRAY
                                            } else if (note.isStarred) {
                                                Color.LIGHTPINK
                                            } else {
                                                Color.LIGHTYELLOW
                                            },
                                            CornerRadii(10.0),
                                            Insets.EMPTY)
                                    )
                                }
                            )
                        }
                    }
                }
            ).apply {
                setFitToWidth(true)
                hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            }
        }

        // Function for updating the current view
        fun updateView() {
            if (showList) {
                root.center = listview()
            } else {
                root.center = gridview()
            }
        }

        // Add EventHandlers to toolbar items
        listButton.onAction = EventHandler {
            showList = true
            root.center = listview()
        }
        gridButton.onAction = EventHandler {
            showList = false
            root.center = gridview()
        }
        showCheckbox.selectedProperty().addListener { _, _, newValue ->
            showArchived = newValue
            updateView()
        }
        orderChoice.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            order = newValue
            updateView()
        }
        clearButton.onAction = EventHandler {
            notes = mutableListOf()
            updateView()
            root.bottom = statusbar()
        }

        // Set scene
        root.apply {
            top = toolbar
            center = listview()
            bottom = statusbar()
        }
        stage.apply {
            minWidth = 640.0;
            minHeight = 480.0;
            scene = Scene(root, 800.0, 600.0)
        }
        stage.show()
    }
}
