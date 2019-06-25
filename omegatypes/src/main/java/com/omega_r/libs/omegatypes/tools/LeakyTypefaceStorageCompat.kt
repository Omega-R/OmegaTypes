package com.omega_r.libs.omegatypes.tools

import android.graphics.Typeface
import android.os.Parcel
import android.os.Process
import java.util.ArrayList

object LeakyTypefaceStorageCompat {

    private val lock = Any()

    private val storage = ArrayList<Typeface>()

    /**
     * Write typeface to parcel.
     *
     * You can't transfer Typeface to a different process. [readTypefaceFromParcel] will
     * return `null` if the [readTypefaceFromParcel] is called in a different process.
     *
     * @param typeface A [Typeface] to be written.
     * @param parcel A [Parcel] object.
     */
    fun writeTypefaceToParcel(typeface: Typeface, parcel: Parcel) {
        parcel.writeInt(Process.myPid())
        synchronized(lock) {
            val id: Int
            val i = storage.indexOf(typeface)
            if (i != -1) {
                id = i
            } else {
                id = storage.size
                storage.add(typeface)
            }
            parcel.writeInt(id)
        }
    }

    /**
     * Read typeface from parcel.
     *
     * If the [Typeface] was created in another process, this method returns null.
     *
     * @param parcel A [Parcel] object
     * @return A [Typeface] object.
     */
    fun readTypefaceFromParcel(parcel: Parcel): Typeface? {
        val pid = parcel.readInt()
        val typefaceId = parcel.readInt()
        if (pid != Process.myPid()) {
            return null  // The Typeface was created and written in another process.
        }
        synchronized(lock) {
            return storage[typefaceId]
        }
    }
}