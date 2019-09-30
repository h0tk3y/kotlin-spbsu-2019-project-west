package core.models

interface UserSupplier {
    fun getUsers(): List<User>
}