package com.lonx.ecjtu.hjcalendar.util

import android.os.Parcel
import android.os.Parcelable


class CourseData {
    data class CourseInfo(
        val courseName: String,
        val courseTime: String = "N/A",
        val courseWeek: String = "N/A",
        val courseLocation: String = "N/A",
        val courseTeacher: String = "N/A"
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "N/A",
            parcel.readString() ?: "N/A",
            parcel.readString() ?: "N/A",
            parcel.readString() ?: "N/A",
            parcel.readString() ?: "N/A"
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(courseName)
            parcel.writeString(courseTime)
            parcel.writeString(courseWeek)
            parcel.writeString(courseLocation)
            parcel.writeString(courseTeacher)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<CourseInfo> {
            override fun createFromParcel(parcel: Parcel): CourseInfo {
                return CourseInfo(parcel)
            }

            override fun newArray(size: Int): Array<CourseInfo?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class DayCourses(
        val date: String,
        val courses: List<CourseInfo>
    )

}

