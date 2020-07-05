package views

interface InitFunction <T> {
    fun getInitial(): T.() -> Unit
}