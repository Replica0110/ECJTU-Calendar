package com.lonx.ecjtu.calendar.domain.usecase.calendar

import com.lonx.ecjtu.calendar.data.datasource.remote.JwxtDataSource
import com.lonx.ecjtu.calendar.data.parser.HtmlParser
import java.net.URL

class GetAcademicCalendarUseCase(private val repository: JwxtDataSource,private val parser: HtmlParser) {
    suspend operator fun invoke(url: String): Result<String> {
        val result = repository.fetchHtml(url).fold(
            onSuccess = { htmlContent ->
                val imageUrl = parser.parseAcademicCalendarImageUrl(htmlContent)
                if (imageUrl != null) {
                    val fullImageUrl = if (imageUrl.startsWith("http")) {
                        imageUrl
                    } else {
                        // 处理相对路径
                        URL(URL(url), imageUrl).toString()
                    }
                    Result.success(fullImageUrl)
                } else {
                    Result.failure(Exception("未能从网页中解析到图片链接"))
                }
            },
            onFailure = {
                Result.failure(it)
            }
        )
        return result
    }
}