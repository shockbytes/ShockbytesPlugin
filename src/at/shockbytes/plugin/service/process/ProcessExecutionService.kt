package at.shockbytes.plugin.service.process

import io.reactivex.Completable
import io.reactivex.Single

interface ProcessExecutionService {

    fun executeCommand(command: String): Single<String>

    fun executeCommands(commands: List<String>): Single<List<String>>

    fun executeCommandOnly(command: String): Completable

    fun executeCommandAndReturnProcess(command: String): Single<Pair<String, Process>>

    fun executeCommandsGrabLastOutput(commands: List<String>): Single<String>

}