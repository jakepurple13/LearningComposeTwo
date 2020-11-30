package anime

import GenericData
import GenericInfo
import GenericInformation
import GenericUi
import RowData
import manga.NineAnime
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class ShowInfo(val name: String, override val url: String, val sources: Sources) : GenericData(name, url) {
    override val listOfData: GenericInformation? get() = getEpisodeInfo()

    fun getEpisodeInfo() = sources.getEpisodeInfo(this)
    internal val extras = mutableMapOf<String, Any?>()
}

data class Episode(
    val source: ShowInfo,
    val name: String,
    override val description: String,
    val image: String?,
    override val genres: List<String>,
    val episodes: List<EpisodeInfo>
) : GenericInformation(name, source.url, image, description, genres), GenericUi by AnimeUi {

    override fun rowData(): List<RowData> =
        episodes.map { RowData(it.name, it.url) { Sources.GOGOANIME.getVideoLink(it).map { it.link.orEmpty() } } }
}

class EpisodeInfo(val name: String, val url: String, private val sources: Sources) {
    fun getVideoLink() = sources.getVideoLink(this)
    override fun toString(): String = "EpisodeInfo(name=$name, url=$url)"
}

internal class NormalLink(var normal: Normal? = null)
internal class Normal(var storage: Array<Storage>? = emptyArray())
data class Storage(
    var sub: String? = null,
    var source: String? = null,
    var link: String? = null,
    var quality: String? = null,
    var filename: String? = null
)

interface ShowApiService : GenericInfo {
    val baseUrl: String
    val canScroll: Boolean get() = false
    fun getRecent(page: Int = 1): List<ShowInfo>
    fun getList(page: Int = 1): List<ShowInfo>
    fun searchList(text: CharSequence, list: List<ShowInfo>): List<ShowInfo>
    fun getEpisodeInfo(source: ShowInfo): Episode?
    fun getVideoLink(info: EpisodeInfo): List<Storage>
}

enum class Sources(private val api: ShowApi) : ShowApiService by api {
    GOGOANIME(GogoAnimeApi),
    //ANIMETOON(AnimeToonApi), DUBBED_ANIME(AnimeToonDubbed), ANIMETOON_MOVIES(AnimeToonMovies),

    //PUTLOCKER(PutLocker), PUTLOCKER_RECENT(PutLockerRecent);
    //KISSANIMEFREE(KissAnimeFree),
    //KICKASSANIME(KickAssAnime)
    //ANIMEFLIX(AnimeFlix)
    ;

    companion object {
        fun getSourceByUrl(url: String) = values().find { url.contains(it.name, true) }
        //fun getAll() = values().flatMap(Sources::getList)

        //fun getAllRecent() = arrayOf(GOGOANIME_RECENT, ANIMETOON_RECENT, PUTLOCKER_RECENT).flatMap(Sources::getList)
        //operator fun get(vararg sources: Sources) = sources.flatMap(Sources::getList)
    }
}

internal fun String.toJsoup() = Jsoup.connect(this).get()

abstract class ShowApi(
    override val baseUrl: String,
    internal val allPath: String,
    internal val recentPath: String
) : ShowApiService {
    //TODO: Add page
    private fun recent(page: Int = 1) = "$baseUrl/$recentPath${recentPage(page)}".toJsoup()
    private fun all(page: Int = 1) = "$baseUrl/$allPath${allPage(page)}".toJsoup()

    internal open fun recentPage(page: Int): String = ""
    internal open fun allPage(page: Int): String = ""

    internal abstract fun getRecent(doc: Document): List<ShowInfo>
    internal abstract fun getList(doc: Document): List<ShowInfo>

    override fun searchList(text: CharSequence, list: List<ShowInfo>): List<ShowInfo> =
        if (text.isEmpty()) list else list.filter { it.name.contains(text, true) }

    override fun getRecent(page: Int) = getRecent(recent(page))

    override fun getList(page: Int) = getList(all(page)).sortedBy(ShowInfo::name)

    override fun getEpisodeInfo(source: ShowInfo): Episode? = getEpisodeInfo(source, source.url.toJsoup())

    internal abstract fun getEpisodeInfo(source: ShowInfo, doc: Document): Episode?
}