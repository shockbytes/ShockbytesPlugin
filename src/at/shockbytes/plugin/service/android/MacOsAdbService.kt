package at.shockbytes.plugin.service.android

import io.reactivex.Single

class MacOsAdbService : AdbService {

    override fun connectToDevice(deviceIp: String, port: Int): Single<Pair<String, String>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun connectToWearable(port: Int): Single<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun disconnect(): Single<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun discoverDeviceIp(): Single<Pair<String, String?>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun restartAdbServer(): Single<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun startScreenCapturing(): Single<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stopScreenCapturing(filePath: String): Single<Pair<String, String>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}