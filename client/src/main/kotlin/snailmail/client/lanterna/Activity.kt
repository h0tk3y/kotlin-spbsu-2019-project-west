package snailmail.client.lanterna

abstract class Activity(val parent: LanternaClient) {
    abstract fun setup()
    abstract fun run()
}