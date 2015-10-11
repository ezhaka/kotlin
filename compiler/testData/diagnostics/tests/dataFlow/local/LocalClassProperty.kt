fun test(x: Any?) {
  if (x !is String) return

  class C {
    val v = <!DEBUG_INFO_SMARTCAST!>x<!>.size

    val vGet: Int
      get() = <!DEBUG_INFO_SMARTCAST!>x<!>.size

    val s: String = <!DEBUG_INFO_SMARTCAST!>x<!>
  }
}
