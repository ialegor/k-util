package io.github.ialegor.util.mutli.github

import com.fasterxml.jackson.annotation.JsonProperty
import feign.Param
import feign.QueryMap
import feign.RequestLine
import io.github.ialegor.util.collection.FuturePage
import io.github.ialegor.util.collection.PageRequest
import io.github.ialegor.util.collection.PageResponse
import io.github.ialegor.util.http.HttpClientBuilder
import io.github.ialegor.util.http.feign.buildFeignClient
import io.github.ialegor.util.logging.log
import io.github.ialegor.util.logging.measure
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class GitHubMultiTest {


    lateinit var gitHubDao: GitHubDao

    @BeforeEach
    fun before() {
        gitHubDao = GitHubDao(
            HttpClientBuilder()
                .url("https://api.github.com")
                .buildFeignClient()
        )
    }

    @Test
    fun testApi() {
        val futureTags = gitHubDao.getRepositoryTagsFuture("spring-projects/spring-boot")

        val allTags = futureTags.getPage(1, 15)

        assertNotNull(allTags)
    }
}

interface GitHubClient {

    @RequestLine("GET /repos/{name}/tags")
    fun getRepositoryTags(@Param("name") name: String, @QueryMap query: Map<String, Any>): List<GitHubTag>
}

class GitHubDao(
    private val client: GitHubClient,
) {
    private val log = log()
    private val options = FuturePage.Options(1, 30, 100)

    fun getRepositoryTags(
        name: String,
        page: PageRequest,
    ): List<GitHubTag> {
        val query = mapOf(
            "page" to page.page, "per_page" to page.size
        )
        return log
            .measure("Get repository tags: $page")
            .extract { client.getRepositoryTags(name, query) }
            .summary { "received $size items" }
    }

    fun getRepositoryTagsFuture(name: String, size: Int = options.defaultSize): FuturePage<GitHubTag> {
        return FuturePage(size, options) {request ->
            val items = getRepositoryTags(name, request)
            return@FuturePage PageResponse(request, items)
        }
    }
}

class GitHubTag {
    @JsonProperty("name")
    lateinit var name: String

    @JsonProperty("zipball_url")
    lateinit var zipballUrl: String

    @JsonProperty("tarball_url")
    lateinit var tarballUrl: String

    @JsonProperty("commit")
    lateinit var commit: GitHubCommit

    @JsonProperty("node_id")
    lateinit var nodeId: String
}

class GitHubCommit {
    @JsonProperty("sha")
    lateinit var sha: String

    @JsonProperty("url")
    lateinit var url: String
}