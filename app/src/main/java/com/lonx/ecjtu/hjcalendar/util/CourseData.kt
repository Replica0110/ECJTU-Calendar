package com.lonx.ecjtu.hjcalendar.util

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


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
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "N/A",
            parcel.createTypedArrayList(CourseInfo.CREATOR) ?: emptyList()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(date)
            parcel.writeTypedList(courses)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<DayCourses> {
            override fun createFromParcel(parcel: Parcel): DayCourses {
                return DayCourses(parcel)
            }

            override fun newArray(size: Int): Array<DayCourses?> {
                return arrayOfNulls(size)
            }
        }
    }
}
fun main(){
    val gson = Gson()


    val dayCourses = CourseData.DayCourses("2024-09-12", listOf(
        CourseData.CourseInfo("Math", "10:00", "Week 1", "Room 101", "Dr. Smith"),
        CourseData.CourseInfo("Science", "11:00", "Week 1", "Room 102", "Dr. Jones")
    ))
    val json = gson.toJson(dayCourses)
    println(json)

    val type = object : TypeToken<CourseData.DayCourses>() {}.type
    val deserializedDayCourses: CourseData.DayCourses = gson.fromJson(json, type)
    println(deserializedDayCourses.courses)
    val tomorrowList: ArrayList<CourseData.CourseInfo> = ArrayList()
    for (course in deserializedDayCourses.courses) {
        tomorrowList.add(course)
    }
    println("tomorrowList: $tomorrowList")
}
