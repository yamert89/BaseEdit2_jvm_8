import javafx.scene.control.TableCell
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.Callback
import javafx.util.StringConverter
import tornadofx.SmartTableCell

/*
class EditableSmartTableCell<S, T>(
    owningColumn: javafx.scene.control.TableColumn<S, T>, converter: StringConverter<S>,
    private val textFieldTableCell: Callback<javafx.scene.control.TableColumn<T, S>, TableCell<T, S>> = TextFieldTableCell.forTableColumn<T, S>(converter) ) :
    SmartTableCell<S, T>(owningColumn = owningColumn){


    override fun startEdit() {
        textFieldTableCell.call(owningColumn).startEdit()
    }

    override fun cancelEdit() {
        textFieldTableCell.cancelEdit()
    }

    override fun updateItem(item: T, empty: Boolean) {
        textFieldTableCell.updateItem(item, empty)
    }
}*/
