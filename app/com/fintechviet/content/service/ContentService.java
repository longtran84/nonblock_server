package com.fintechviet.content.service;

import com.fintechviet.ad.dto.Content;
import com.fintechviet.ad.dto.Decision;
import com.fintechviet.ad.dto.DecisionResponse;
import com.fintechviet.ad.model.Ad;
import com.fintechviet.ad.repository.AdvertismentRepository;
import com.fintechviet.content.dto.News;
import com.fintechviet.content.dto.NewsCategory;
import com.fintechviet.content.model.Game;
import com.fintechviet.content.respository.ContentRepository;
import com.fintechviet.utils.CommonUtils;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jsoup.Jsoup;
import play.Configuration;
import play.Logger;

import static java.util.concurrent.CompletableFuture.supplyAsync;


public class ContentService {
	private final ContentRepository contentRepository;
	private final AdvertismentRepository advertismentRepository;
	static final int limitResult = 500;
	//private static String CRAWLER_ENPOINT = "http://192.168.100.107:3689/solr/Crawler";
	private static String CRAWLER_ENPOINT = "http://222.252.16.132:3689/solr/Crawler";
	private static String DOMAIN = "http://222.252.16.132:9000";
	private static String ID = "id";
	private static String CATEGORY_CODE = "MaChuyenMuc";
	private static String SOURCE_NAME = "TenMien";
	private static String TITLE = "TieuDe";
	private static String CONTENT = "NoiDung";
	private static String LINK = "DuongDan";
	private static String IMAGE_LINK = "AnhDaiDien";
	private static String PUBLISH_DATE = "NgayDangTin";
	private static String CRAWLER_DATE = "NgayCrawler";

	@Inject
	private Configuration configuration;

	@Inject
	public ContentService(ContentRepository contentRepository, AdvertismentRepository advertismentRepository){
		this.contentRepository = contentRepository;
		this.advertismentRepository = advertismentRepository;
	}

	public CompletionStage<String> saveImpression() {
		return contentRepository.saveImpression();
	}

	public CompletionStage<String> saveClick() {
		return contentRepository.saveClick();
	}

	private List<News> convertToDto(List<com.fintechviet.content.model.News> newsModelList){
		List<News> newsDtoList = new ArrayList<>();
		for(com.fintechviet.content.model.News news : newsModelList) {
			News neDTO =  new News();
			neDTO.setId(news.getId());
			neDTO.setTitle(news.getTitle());
			neDTO.setShortDescription(news.getShortDescription());
			neDTO.setImageLink(news.getImageLink());
			neDTO.setLink(news.getLink());
			neDTO.setNewsCategoryCode(news.getNewsCategory().getCode());
			neDTO.setCreatedDate(news.getCreatedDate());
			newsDtoList.add(neDTO);
		}
		return newsDtoList;
	}

	public CompletionStage<List<NewsCategory>> getCategoriesList() throws InterruptedException, ExecutionException{
		CompletableFuture<List<com.fintechviet.content.model.NewsCategory>> cateListFuture =
				supplyAsync(() -> contentRepository.getAllCategories());
		List<com.fintechviet.content.model.NewsCategory> cateList = cateListFuture.get();
		return supplyAsync(() -> convertCategoriesToDto(cateList));

	}

	private List<NewsCategory> convertCategoriesToDto(List<com.fintechviet.content.model.NewsCategory> categoriesList){
		List<NewsCategory> categoryDtoList = new ArrayList<>();
		for(com.fintechviet.content.model.NewsCategory cate : categoriesList) {
			NewsCategory cateDto =  new NewsCategory();
			cateDto.setCode(cate.getCode());
			cateDto.setName(cate.getName());
			cateDto.setImageFile(cate.getImage());
			cateDto.setId(cate.getId());
			categoryDtoList.add(cateDto);
		}
		return categoryDtoList;
	}

	/**
	 *
	 * @param deviceToken
	 * @param fromDate
	 * @param toDate
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public CompletionStage<List<News>> getNewsByUserInterest(String deviceToken, Date fromDate, Date toDate) throws InterruptedException, ExecutionException {
		String parentThreadName = Thread.currentThread().getName();
		System.out.println("["+ parentThreadName +"] Started ");
		long t0 = System.currentTimeMillis();
		List<Long> categoryList = contentRepository.getNumberOfUserInterest(deviceToken);
		List<News> newList = new ArrayList<>();
		List<CompletableFuture<List<com.fintechviet.content.model.News>>> pendingTask = new ArrayList<>();
		for(int i =0; i< categoryList.size(); i++){
			Long cateId = categoryList.get(i);
			CompletableFuture<List<com.fintechviet.content.model.News>> newsTrunkFuture =
					supplyAsync(() -> contentRepository.getNewsByUserInterest(deviceToken, cateId, fromDate, toDate));
			pendingTask.add(newsTrunkFuture);
		}
		for(CompletableFuture<List<com.fintechviet.content.model.News>> futureTask: pendingTask){
			newList.addAll(convertToDto(futureTask.get()));
		}
		long t1 = System.currentTimeMillis();
		System.out.println("["+ parentThreadName +"]Total time: " + (t1 - t0) );
		return supplyAsync(() -> newList);
	}

	private List<News> buildNewsList(SolrDocumentList results) {
		List<News> newsList = new ArrayList<>();
		for (SolrDocument document : results) {
			News news = new News();
			for (Iterator<Map.Entry<String, Object>> i = document.iterator(); i.hasNext();) {
				Map.Entry<String, Object> element = i.next();
				String key = element.getKey().toString();
				String value = element.getValue().toString();
				if (ID.equals(key)) {
					news.setId(Long.valueOf(value));
				} else if(CATEGORY_CODE.equals(key)) {
					news.setNewsCategoryCode(value);
				} else if(TITLE.equals(key)) {
					news.setTitle(value);
				} else if(CONTENT.equals(key)) {
					news.setShortDescription(Jsoup.parse(value).text());
				} else if(LINK.equals(key)) {
					news.setLink(value);
				} else if(IMAGE_LINK.equals(key)) {
					value = value.replaceAll("localhost", "222.252.16.132");
					news.setImageLink(value);
				} else if(CRAWLER_DATE.equals(key)) {
					news.setCreatedDate((Date)element.getValue());
				}
			}
			newsList.add(news);
		}
		return newsList;
	}

	private List<News> buildNewsListAndAdv(SolrDocumentList results, String deviceToken) {
		List<News> newsList = new ArrayList<>();
		for (SolrDocument document : results) {
			News news = new News();
			for (Iterator<Map.Entry<String, Object>> i = document.iterator(); i.hasNext();) {
				Map.Entry<String, Object> element = i.next();
				String key = element.getKey().toString();
				String value = element.getValue().toString();
				if (ID.equals(key)) {
					news.setId(Long.valueOf(value));
				} else if(CATEGORY_CODE.equals(key)) {
					news.setNewsCategoryCode(value);
				} else if(TITLE.equals(key)) {
					news.setTitle(value);
				} else if(CONTENT.equals(key)) {
					news.setShortDescription(Jsoup.parse(value).text());
				} else if(LINK.equals(key)) {
					news.setLink(value);
				} else if(IMAGE_LINK.equals(key)) {
					value = value.replaceAll("localhost", "222.252.16.132");
					news.setImageLink(value);
				} else if(CRAWLER_DATE.equals(key)) {
					news.setCreatedDate((Date)element.getValue());
				}
			}
			newsList.add(news);
		}
		int advNumber = Integer.parseInt(configuration.getString("adv.display.number"));
		Random rd = new Random();
		for (int i = 0; i < advNumber; i++) {
			int templateIndex = rd.nextInt(advNumber);
			int rows = Integer.parseInt(configuration.getString("news.rows"));
			int index = rd.nextInt(rows);
			String template;
			if (templateIndex == 0) {
				template = "image";
			} else {
				template = "video";
			}
			Ad ad;
			if (template.equals("image")) {
				ad = advertismentRepository.getAdByTemplate(template, 1);
			} else {
				ad = advertismentRepository.getAdByTemplate(template, 0);
			}

			DecisionResponse adv = buildAdResponse(ad, template, deviceToken);
			if (index <= newsList.size() - 1) {
				News news = newsList.get(index);
				news.setDecisionResponse(adv);
			}
		}
		return newsList;
	}

	private DecisionResponse buildAdResponse(Ad ad, String template, String deviceToken) {
		if (ad != null) {
			DecisionResponse response = new DecisionResponse();
			Decision decision = new Decision();
			Content content = new Content();
			decision.setAdId(ad.getId());
			decision.setClickUrl(ad.getCreative().getClickUrl());
			if (template.equals("image")) {
				decision.setTrackingUrl(DOMAIN + "/ad/click?adId=" + ad.getId() + "&deviceToken=" + deviceToken);
				content.setImageUrl(ad.getCreative().getImageLink());;
			} else {
				decision.setViewUrl(DOMAIN + "/ad/view?adId=" + ad.getId() + "&deviceToken=" + deviceToken);
				content.setVideoUrl(ad.getCreative().getVideoLink());
			}
			decision.setImpressionUrl(DOMAIN + "/ad/impression/" + ad.getId());
			content.setBody(ad.getCreative().getBody());
			content.setTemplate(template);
			decision.setContent(content);
			response.setDecision(decision);
			return response;
		}

		return null;
	}

	private List<News> getNewsFromCrawler(String deviceToken, List<com.fintechviet.content.model.NewsCategory> newsCategries, Integer pageIndex) {
//		String startTime = DateUtils.convertDateToStringUTC(fromDate);
//		String endTime = DateUtils.convertDateToStringUTC(toDate);
		List<String> interests = new ArrayList<String>();
		for (com.fintechviet.content.model.NewsCategory newsCategory : newsCategries) {
			interests.add(newsCategory.getCode());
		}
		List<News> newsList = new ArrayList<>();
		try {
			int rows = Integer.parseInt(configuration.getString("news.rows"));
			SolrClient client = new HttpSolrClient.Builder(CRAWLER_ENPOINT).build();
			String queryStr = "*.*";
			if (!interests.isEmpty()) {
				queryStr = CATEGORY_CODE + ":" + CommonUtils.convertListToString(interests);
			}
			SolrQuery query = new SolrQuery();
			query.setQuery(queryStr);
//			String filterQueryStr = CRAWLER_DATE + ":[" + startTime + " TO " + endTime + "]";
//			filterQueryStr = filterQueryStr.replaceAll("\\+", "");
//			query.addFilterQuery(filterQueryStr);
			query.setFields(ID, CATEGORY_CODE, SOURCE_NAME, TITLE, CONTENT, LINK, IMAGE_LINK, PUBLISH_DATE, CRAWLER_DATE);
			query.setStart((pageIndex - 1) * rows);
			query.setRows(rows);
			query.setSort(CRAWLER_DATE, SolrQuery.ORDER.desc);
			query.set("defType", "edismax");

			QueryResponse response = client.query(query);
			SolrDocumentList results = response.getResults();
			newsList = buildNewsListAndAdv(results, deviceToken);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return newsList;
	}

	private List<News> getNewsFromCrawler1(String interests, Integer pageIndex) {
//		String startTime = DateUtils.convertDateToStringUTC(fromDate);
//		String endTime = DateUtils.convertDateToStringUTC(toDate);
		List<News> newsList = new ArrayList<>();
		try {
			int rows = Integer.parseInt(configuration.getString("news.rows"));
			SolrClient client = new HttpSolrClient.Builder(CRAWLER_ENPOINT).build();
			//String queryStr = CATEGORY_CODE + ":" + CommonUtils.convertListToString(interests);
			String queryStr = CATEGORY_CODE + ":" + interests;
			SolrQuery query = new SolrQuery();
			query.setQuery(queryStr);
//			String filterQueryStr = CRAWLER_DATE + ":[" + startTime + " TO " + endTime + "]";
//			filterQueryStr = filterQueryStr.replaceAll("\\+", "");
//			query.addFilterQuery(filterQueryStr);
			query.setFields(ID, CATEGORY_CODE, SOURCE_NAME, TITLE, CONTENT, LINK, IMAGE_LINK, PUBLISH_DATE, CRAWLER_DATE);
			query.setStart((pageIndex - 1) * rows);
			query.setRows(rows);
			query.setSort(CRAWLER_DATE, SolrQuery.ORDER.desc);
			query.set("defType", "edismax");

			QueryResponse response = client.query(query);
			SolrDocumentList results = response.getResults();
			newsList = buildNewsList(results);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return newsList;
	}

	private List<News> getNewsByCategory(String categoryCode) {
		List<News> newsList = new ArrayList<News>();
		try {
			SolrClient client = new HttpSolrClient.Builder(CRAWLER_ENPOINT).build();
			SolrQuery query = new SolrQuery();
			String queryStr = CATEGORY_CODE + ":" + categoryCode;
			query.setQuery(queryStr);
			query.setFields(ID, CATEGORY_CODE, SOURCE_NAME, TITLE, CONTENT, LINK, IMAGE_LINK, PUBLISH_DATE, CRAWLER_DATE);
			query.setStart(0);
			query.setRows(20);
			query.setSort(CRAWLER_DATE, SolrQuery.ORDER.desc);
			query.set("defType", "edismax");

			QueryResponse response = client.query(query);
			SolrDocumentList results = response.getResults();
			newsList = buildNewsList(results);
		} catch(Exception ex) {
			ex.printStackTrace();
		}

		return newsList;
	}

	private List<News> getTopNews(List<com.fintechviet.content.model.NewsCategory> newsCategries) {
		List<News> newsList = new ArrayList<News>();
		try {
			List<String> interests = new ArrayList<String>();
			for (com.fintechviet.content.model.NewsCategory newsCategory : newsCategries) {
				interests.add(newsCategory.getCode());
			}
			SolrClient client = new HttpSolrClient.Builder(CRAWLER_ENPOINT).build();
			SolrQuery query = new SolrQuery();
			String queryStr = CATEGORY_CODE + ":" + CommonUtils.convertListToString(interests);
			query.setQuery(queryStr);
			query.setFields(ID, CATEGORY_CODE, SOURCE_NAME, TITLE, CONTENT, LINK, IMAGE_LINK, PUBLISH_DATE, CRAWLER_DATE);
			query.setStart(0);
			query.setRows(50);
			query.setSort(CRAWLER_DATE, SolrQuery.ORDER.desc);
			query.set("defType", "edismax");

			QueryResponse response = client.query(query);
			SolrDocumentList results = response.getResults();
			newsList = buildNewsList(results);
		} catch(Exception ex) {
			ex.printStackTrace();
		}

		return newsList;
	}

	private List<News> getAdNews(Integer pageIndex) {
		List<News> newsList = new ArrayList<News>();
		try {
			int rows = Integer.parseInt(configuration.getString("news.rows"));
			SolrClient client = new HttpSolrClient.Builder(CRAWLER_ENPOINT).build();
			SolrQuery query = new SolrQuery();
			String queryStr = CATEGORY_CODE + ":" + "TaiChinh";
			query.setQuery(queryStr);
			query.setFields(ID, CATEGORY_CODE, SOURCE_NAME, TITLE, CONTENT, LINK, IMAGE_LINK, PUBLISH_DATE, CRAWLER_DATE);
			query.setStart((pageIndex - 1) * rows);
			query.setRows(rows);
			query.setSort(CRAWLER_DATE, SolrQuery.ORDER.desc);
			query.set("defType", "edismax");

			QueryResponse response = client.query(query);
			SolrDocumentList results = response.getResults();
			newsList = buildNewsList(results);
		} catch(Exception ex) {
			ex.printStackTrace();
		}

		return newsList;
	}

	private List<News> getNewsById(String newsId) {
		List<News> newsList = new ArrayList<>();
		try {
			SolrClient client = new HttpSolrClient.Builder(CRAWLER_ENPOINT).build();
			String queryStr = ID + ":" + newsId;
			SolrQuery query = new SolrQuery();
			query.setQuery(queryStr);
			query.setFields(ID, CATEGORY_CODE, SOURCE_NAME, TITLE, CONTENT, LINK, IMAGE_LINK, PUBLISH_DATE, CRAWLER_DATE);
			query.setStart(0);
			query.setRows(1);
			query.setSort(CRAWLER_DATE, SolrQuery.ORDER.desc);
			query.set("defType", "edismax");

			QueryResponse response = client.query(query);
			SolrDocumentList results = response.getResults();
			newsList = buildNewsList(results);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return newsList;
	}


	/**
	 *
	 * @param deviceToken
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public CompletionStage<List<News>> getNewsByUserInterestFromCrawler(String deviceToken, Integer page, String newsId) throws InterruptedException, ExecutionException {
		Logger.info("######################### Start get news for user #######################");
		List<com.fintechviet.content.model.NewsCategory> categoryList = contentRepository.getUserInterests(deviceToken);
		List<News> newsList = new ArrayList<News>();
		if (newsId != null && !newsId.equals("")) {
			newsList.addAll(getNewsById(newsId));
		}
		List<News> newsByCategory = getNewsFromCrawler(deviceToken, categoryList, page);
		for (News news : newsByCategory) {
			if (newsId != null && !newsId.equals("")) {
				if (news.getId() != Long.parseLong(newsId))
					newsList.add(news);
			} else {
				newsList.add(news);
			}
		}
		Logger.info("######################### End get news for user #######################");
		return supplyAsync(() -> newsList);
	}

	/**
	 *
	 * @param interests
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public CompletionStage<List<News>> getNewsByUserInterestFromCrawler1(String interests, Integer page) throws InterruptedException, ExecutionException {
		return supplyAsync(() -> getNewsFromCrawler1(interests, page));
	}

	/**
	 *
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public CompletionStage<List<News>> getTopNewsByCategory(String categoryCode) throws InterruptedException, ExecutionException {
		return supplyAsync(() -> getNewsByCategory(categoryCode));
	}

	/**
	 *
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public CompletionStage<List<News>> getNewsOnLockScreen(String deviceToken) throws InterruptedException, ExecutionException {
		List<com.fintechviet.content.model.NewsCategory> categoryList = contentRepository.getUserInterests(deviceToken);
		return supplyAsync(() -> getTopNews(categoryList));
	}

	/**
	 *
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public CompletionStage<List<Game>> getGames() throws InterruptedException, ExecutionException {
		return contentRepository.getGames();
	}

	/**
	 *
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public CompletionStage<List<News>> getListAdNews(Integer pageIndex) throws InterruptedException, ExecutionException {
		return supplyAsync(() -> getAdNews(pageIndex));
	}
}
