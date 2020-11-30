package manga

import GenericData
import GenericInfo
import GenericInformation
import GenericUi
import RowData

interface MangaSource : GenericInfo {
    val websiteUrl: String
    val hasMorePages: Boolean
    val headers: List<Pair<String, String>> get() = emptyList()
    fun getManga(pageNumber: Int = 1): List<MangaModel>
    fun toInfoModel(model: MangaModel): MangaInfoModel
    fun getMangaModelByUrl(url: String): MangaModel
    fun getPageInfo(chapterModel: ChapterModel): PageModel
    fun searchManga(searchText: CharSequence, pageNumber: Int = 1, mangaList: List<MangaModel>): List<MangaModel> =
        mangaList.filter { it.title.contains(searchText, true) }
}

data class MangaModel(
    override val title: String,
    val description: String,
    val mangaUrl: String,
    val imageUrl: String,
    val source: Sources
) : GenericData(title, mangaUrl) {
    internal val extras = mutableMapOf<String, Any>()
    fun toInfoModel() = source.toInfoModel(this)

    override val listOfData: GenericInformation? get() = toInfoModel()
}

data class MangaInfoModel(
    override val title: String,
    override val description: String,
    val mangaUrl: String,
    override val imageUrl: String,
    val chapters: List<ChapterModel>,
    override val genres: List<String>,
    val alternativeNames: List<String>
): GenericInformation(title, mangaUrl, imageUrl, description, genres), GenericUi by MangaUi {
    override fun rowData(): List<RowData> = chapters.map { RowData(it.name, it.uploaded) { it.getPageInfo().pages } }
}

data class ChapterModel(
    val name: String,
    val url: String,
    val uploaded: String,
    val sources: Sources
) {
    var uploadedTime: Long? = null
    fun getPageInfo() = sources.getPageInfo(this)
}

data class PageModel(val pages: List<String>)

enum class Sources(
    val domain: String,
    val isAdult: Boolean = false,
    val filterOutOfUpdate: Boolean = false,
    val source: MangaSource
) : MangaSource by source {

    //MANGA_EDEN(domain = "mangaeden", filterOutOfUpdate = true, source = MangaEden),
    //MANGANELO(domain = "manganelo", source = Manganelo),
    //MANGA_HERE(domain = "mangahere", source = MangaHere),
    //MANGA_4_LIFE(domain = "manga4life", source = MangaFourLife),
    NINE_ANIME(domain = "nineanime", source = NineAnime),
    //MANGAKAKALOT(domain = "mangakakalot", source = Mangakakalot),
    //MANGAMUTINY(domain = "mangamutiny", source = Mangamutiny),
    //MANGA_PARK(domain = "mangapark", source = MangaPark),

    //MANGA_DOG(domain = "mangadog", source = MangaDog),
    //INKR(domain = "mangarock", source = com.programmersbox.manga_sources.mangasources.manga.INKR),
    //TSUMINO(domain = "tsumino", isAdult = true, source = Tsumino)
    ;

    companion object {
        fun getSourceByUrl(url: String) = values().find { url.contains(it.domain, true) }

        fun getUpdateSearches() = values().filterNot(Sources::isAdult).filterNot(Sources::filterOutOfUpdate)
    }
}