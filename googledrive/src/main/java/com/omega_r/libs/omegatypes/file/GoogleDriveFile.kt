package com.omega_r.libs.omegatypes.file


/**
 * Created by Anton Knyazev on 2019-10-04.
 */
class GoogleDriveFile(private val file: com.google.api.services.drive.model.File) : File {

    companion object {

        private const val MIME_TYPE_FOLDER = "application/vnd.google-apps.folder"

    }

    override val name: String
        get() = file.name

    override val mimeType: String
        get() = file.mimeType

    override val type: File.Type
        get() = if (file.mimeType == MIME_TYPE_FOLDER) File.Type.FOLDER else File.Type.FILE

}