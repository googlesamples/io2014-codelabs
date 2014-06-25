import 'package:polymer/polymer.dart';
import 'model.dart' show Codelab;
import 'dart:html' show Event, Node, CustomEvent;

@CustomTag('codelab-element')
class CodelabElement extends PolymerElement {
  @published Codelab codelab;
  @observable bool editing = false;
  Codelab _cachedCodelab;

  CodelabElement.created(): super.created() {}

  /*
   * Updates codelab. If the codelab's level has changed, dispatches a
   * custom event. This allows the element's parent to register a listener to
   * update the filtered codelabs list.
   */
  updateCodelab(Event e, var detail, Node sender) {
    e.preventDefault();
    if (_cachedCodelab.level != codelab.level) {
      dispatchEvent(new CustomEvent('levelchanged'));
    }
    editing = false;
  }

  /*
   * Cancels editing, restoring the original codelab values.
   */
  cancelEditing(Event e, var detail, Node sender) {
    e.preventDefault();
    copyCodelab(codelab, _cachedCodelab);
    editing = false;
  }

  /*
   * Starts editing, caching the codelab values.
   */
  startEditing(Event e, var detail, Node sender) {
    e.preventDefault();
    _cachedCodelab = new Codelab();
    copyCodelab(_cachedCodelab, codelab);
    editing = true;
  }

  /*
   * Dispatches a custom event requesting the codelab be deleted.
   */
  deleteCodelab(Event e, var detail, Node sender) {
    e.preventDefault();
    dispatchEvent(new CustomEvent('deletecodelab',
        detail: {'codelab': codelab}));
  }

  /*
   * Copies values from source codelab to destination codelab.
   */
  copyCodelab(source, destination) {
    source.title = destination.title;
    source.description = destination.description;
    source.level = destination.level;
  }
}