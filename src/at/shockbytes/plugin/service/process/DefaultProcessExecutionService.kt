package at.shockbytes.plugin.service.process

import at.shockbytes.plugin.util.readProcess
import io.reactivex.Completable
import io.reactivex.Single
import java.io.IOException

class DefaultProcessExecutionService : ProcessExecutionService {

    override fun executeCommandsGrabLastOutput(commands: List<String>): Single<String> {
        return Single.fromCallable {
            for (i in 0 until commands.size - 1) {
                Runtime.getRuntime().exec(commands[i])
            }
            executeCommand(commands.last()).blockingGet()
        }
    }

    override fun executeCommands(commands: List<String>): Single<List<String>> {
        return Single.fromCallable {
            commands.mapTo(mutableListOf()) { executeCommand(commands.last()).blockingGet() }
        }
    }

    override fun executeCommandAndReturnProcess(command: String): Single<Pair<String, Process>> {
        return Single.fromCallable {
            val process = Runtime.getRuntime().exec(command)
            val output = process.readProcess() ?: throw IOException("Cannot read process!")
            Pair(output, process)
        }
    }

    override fun executeCommandOnly(command: String): Completable {
        return Completable.fromAction {
            Runtime.getRuntime().exec(command)
        }
    }

    override fun executeCommand(command: String): Single<String> {
        return Single.fromCallable {
            Runtime.getRuntime().exec(command).readProcess() ?: throw IOException("Cannot read process!")
        }
    }
}