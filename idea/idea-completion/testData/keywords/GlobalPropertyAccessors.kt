fun foo() {
    bar()
}

fun bar() {
    foo()
}

var a : Int
  <caret>

// EXIST:  abstract
// EXIST: by
// EXIST:  class
// EXIST:  enum
// EXIST:  final
// EXIST:  fun
// EXIST: get
// EXIST:  inner
// EXIST:  internal
// EXIST:  object
// EXIST:  open
// EXIST:  private
// EXIST:  protected
// EXIST:  public
// EXIST: set
// EXIST:  interface
// EXIST:  val
// EXIST:  var
// EXIST:  companion object
// EXIST:  operator
// EXIST:  infix
// EXIST:  sealed
// EXIST:  data
// EXIST:  inline
// EXIST:  tailrec
// EXIST:  external
// EXIST:  annotation
// EXIST:  const
// NOTHING_ELSE
