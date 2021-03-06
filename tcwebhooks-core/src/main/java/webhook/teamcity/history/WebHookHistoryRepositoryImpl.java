package webhook.teamcity.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.LocalDate;

public class WebHookHistoryRepositoryImpl implements WebHookHistoryRepository {
	
	MaxSizeHashMap<UUID,WebHookHistoryItem> webHookHistoryItems = new MaxSizeHashMap<>(10000); 
	Comparator<WebHookHistoryItem> chronComparator = new WebHookHistoryRepositoryDateSortingCompator(SortDirection.ASC);
	Comparator<WebHookHistoryItem> reverseChronComparator = new WebHookHistoryRepositoryDateSortingCompator(SortDirection.DESC);
	
	@Override
	public void addHistoryItem(WebHookHistoryItem histoyItem) {
		this.webHookHistoryItems.put(histoyItem.getWebHookExecutionStats().getTrackingId(), histoyItem);
	}
	
	@Override
	public WebHookHistoryItem getHistoryItem(String trackingId) {
		try {
			UUID uuid = UUID.fromString(trackingId);
			return webHookHistoryItems.get(uuid);
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	@Override
	public PagedList<WebHookHistoryItem> findHistoryItemsForProject(String projectId, int pageNumber, int pageSize) {
		List<WebHookHistoryItem> projItems = new ArrayList<>();
		for (WebHookHistoryItem item : webHookHistoryItems.values()) {
			if (item.getProjectId().equals(projectId)) {
				projItems.add(item);
			}
		}
		Collections.sort(projItems, reverseChronComparator);
		return PagedList.build(pageNumber, pageSize, projItems);
	}

	@Override
	public PagedList<WebHookHistoryItem> findHistoryItemsForBuildType(String buildTypeId, int pageNumber, int pageSize) {
		List<WebHookHistoryItem> buildTypeItems = new ArrayList<>();
		for (WebHookHistoryItem item : webHookHistoryItems.values()) {
			if (buildTypeId.equals(item.getBuildTypeId())) {
				buildTypeItems.add(item);
			}
		}
		Collections.sort(buildTypeItems, reverseChronComparator);
		return PagedList.build(pageNumber, pageSize, buildTypeItems);
	}

	@Override
	public PagedList<WebHookHistoryItem> findHistoryItemsForBuild(Long buildId, int pageNumber, int pageSize) {
		List<WebHookHistoryItem> buildItems = new ArrayList<>();
		for (WebHookHistoryItem item : webHookHistoryItems.values()) {
			if (buildId.equals(item.getBuildId())) {
				buildItems.add(item);
			}
		}
		Collections.sort(buildItems, reverseChronComparator);
		return PagedList.build(pageNumber, pageSize, buildItems);
	}
	
	@Override
	public PagedList<WebHookHistoryItem> findHistoryItemsForWebHookConfigId(String webHookId, int pageNumber, int pageSize) {
		List<WebHookHistoryItem> historyItems = new ArrayList<>();
		for (WebHookHistoryItem item : webHookHistoryItems.values()) {
			if (webHookId.equals(item.getWebHookConfig().getUniqueKey())) {
				historyItems.add(item);
			}
		}
		Collections.sort(historyItems, reverseChronComparator);
		return PagedList.build(pageNumber, pageSize, historyItems);
	}
	
	@Override
	public Map<String, Integer> findHistoryItemCountsForWebHookConfigId(String webhookId) {
		List<WebHookHistoryItem> historyItems = new ArrayList<>();
		for (WebHookHistoryItem item : webHookHistoryItems.values()) {
			if (webhookId.equals(item.getWebHookConfig().getUniqueKey())) {
				historyItems.add(item);
			}
		}
		Map<String, Integer> itemCounts = new HashMap<>();
		itemCounts.put("ok", findHistoryItemsOK(historyItems).size());
		itemCounts.put("errored", findHistoryItemsInError(historyItems).size());
		itemCounts.put("disabled", findHistoryItemsDisabled(historyItems).size());
		itemCounts.put("total", historyItems.size());
		return itemCounts;
	}	

	@Override
	public PagedList<WebHookHistoryItem> findHistoryErroredItems(int pageNumber, int pageSize) {
		List<WebHookHistoryItem> errorItems = findHistoryItemsInError(webHookHistoryItems.values());
		Collections.sort(errorItems, reverseChronComparator);
		return PagedList.build(pageNumber, pageSize, errorItems);
	}
	
	@Override
	public PagedList<WebHookHistoryItem> findHistoryDisabledItems(int pageNumber, int pageSize) {
		List<WebHookHistoryItem> disabledItems = findHistoryItemsDisabled(webHookHistoryItems.values());
		Collections.sort(disabledItems, reverseChronComparator);
		return PagedList.build(pageNumber, pageSize, disabledItems);
	}
	
	@Override
	public PagedList<WebHookHistoryItem> findHistoryOkItems(int pageNumber, int pageSize) {
		List<WebHookHistoryItem> okItems =  findHistoryItemsOK(webHookHistoryItems.values());
		Collections.sort(okItems, reverseChronComparator);
		return PagedList.build(pageNumber, pageSize, okItems);
	}

	@Override
	public PagedList<WebHookHistoryItem> findHistoryAllItems(int pageNumber, int pageSize) {
		List<WebHookHistoryItem> allitems = new ArrayList<>();
		allitems.addAll(webHookHistoryItems.values());
		Collections.sort(allitems, reverseChronComparator);
		return PagedList.build(pageNumber, pageSize, allitems);
	}	

	private List<WebHookHistoryItem> findHistoryItemsInError(Collection<WebHookHistoryItem> webHookHistoryItemsCollection) {
		List<WebHookHistoryItem> errorItems = new ArrayList<>();
		for (WebHookHistoryItem item : webHookHistoryItemsCollection) {
			if (item.getWebHookExecutionStats().isErrored()) {
				errorItems.add(item);
			}
		}
		return errorItems;
	}
	
	private List<WebHookHistoryItem> findHistoryItemsDisabled(Collection<WebHookHistoryItem> webHookHistoryItemsCollection) {
		List<WebHookHistoryItem> disabledItems = new ArrayList<>();
		for (WebHookHistoryItem item : webHookHistoryItemsCollection) {
			if (! item.getWebHookExecutionStats().isEnabled()) {
				disabledItems.add(item);
			}
		}
		return disabledItems;
	}
	
	private List<WebHookHistoryItem> findHistoryItemsOK(Collection<WebHookHistoryItem> webHookHistoryItemsCollection) {
		List<WebHookHistoryItem> okItems = new ArrayList<>();
		for (WebHookHistoryItem item : webHookHistoryItemsCollection) {
			if (item.getWebHookExecutionStats().isEnabled()  && ! item.getWebHookExecutionStats().isErrored()) {
				okItems.add(item);
			}
		}
		return okItems;
	}
	
	@Override
	public Map<LocalDate, List<WebHookHistoryItem>> findHistoryDisabledItemsGroupedByDay(LocalDate untilDate, int numberOfDays) {
		boolean errored = false;
		boolean disabled = true;
		return findHistoryItemsGroupedByDay(untilDate, numberOfDays, findItemsSince(untilDate, numberOfDays, errored, disabled));
	}

	@Override
	public Map<LocalDate, List<WebHookHistoryItem>> findHistoryErroredItemsGroupedByDay(LocalDate untilDate, int numberOfDays) {
		boolean errored = true;
		boolean disabled = false;
		return findHistoryItemsGroupedByDay(untilDate, numberOfDays, findItemsSince(untilDate, numberOfDays, errored, disabled));
	}
	
	@Override
	public Map<LocalDate, List<WebHookHistoryItem>> findHistoryOkItemsGroupedByDay(LocalDate untilDate, int numberOfDays) {
		boolean errored = false;
		boolean disabled = false;
		return findHistoryItemsGroupedByDay(untilDate, numberOfDays, findItemsSince(untilDate, numberOfDays, errored, disabled));
	}
	
	@Override
	public Map<LocalDate, List<WebHookHistoryItem>> findHistoryAllItemsGroupedByDay(LocalDate untilDate, int numberOfDays) {
		return findHistoryItemsGroupedByDay(untilDate, numberOfDays, findAllItemsSince(untilDate, numberOfDays));
	}
	
	private Map<LocalDate, List<WebHookHistoryItem>> findHistoryItemsGroupedByDay(LocalDate untilDate, int numberOfDays, List<WebHookHistoryItem> items) {
		Map<LocalDate, List<WebHookHistoryItem>> groupedHistoryItems = new LinkedHashMap<>(numberOfDays);
		for (LocalDate thisDate : getArrayOfDates(untilDate, numberOfDays)) {
			List<WebHookHistoryItem> thisDateList = new ArrayList<>();
			for (WebHookHistoryItem item : items) {
				if (thisDate.isEqual(item.getTimestamp().toLocalDate())) {
					thisDateList.add(item);
				}
			}
			groupedHistoryItems.put(thisDate, thisDateList);
		}
		return groupedHistoryItems;
	}

	private List<WebHookHistoryItem> findItemsSince(LocalDate untilDate, int numberOfDays, boolean isErrored, boolean isDisabled) {
		List<WebHookHistoryItem> itemsSince = new ArrayList<>();
		LocalDate sinceDate = untilDate.minusDays(numberOfDays);
		for (WebHookHistoryItem item : this.webHookHistoryItems.values()) {
			LocalDate itemTimeStamp = item.getTimestamp().toLocalDate(); 
			if (sinceDate.isBefore(itemTimeStamp) 
					&& untilDate.isAfter(itemTimeStamp) 
					&& item.getWebHookExecutionStats().isErrored() == isErrored 
					&& item.getWebHookExecutionStats().isEnabled() != isDisabled) {
				itemsSince.add(item);
			}
		}
		return itemsSince;
	}
	
	private List<WebHookHistoryItem> findAllItemsSince(LocalDate untilDate, int numberOfDays) {
		List<WebHookHistoryItem> erroredItemsSince = new ArrayList<>();
		LocalDate sinceDate = untilDate.minusDays(numberOfDays);
		for (WebHookHistoryItem item : this.webHookHistoryItems.values()) {
			LocalDate itemTimeStamp = item.getTimestamp().toLocalDate(); 
			if (sinceDate.isBefore(itemTimeStamp) && untilDate.isAfter(itemTimeStamp)) {
				erroredItemsSince.add(item);
			}
		}
		return erroredItemsSince;
	}

	private List<LocalDate> getArrayOfDates(LocalDate untilDate, int numberOfDays) {
		List<LocalDate> days = new ArrayList<>();
		for (int i = numberOfDays; i > 0 ; i--) {
			days.add(untilDate.minusDays(i));
		}
		return days;
	}

	@Override
	public int getTotalCount() {
		return webHookHistoryItems.size();
	}


	@Override
	public int getDisabledCount() {
		return findHistoryItemsDisabled(webHookHistoryItems.values()).size();
	}	
	
	@Override
	public int getErroredCount() {
		return findHistoryItemsInError(webHookHistoryItems.values()).size();
	}

	@Override
	public int getOkCount() {
		return webHookHistoryItems.size() - ( getErroredCount() + getDisabledCount() );
	}

	public class WebHookHistoryRepositoryDateSortingCompator implements Comparator<WebHookHistoryItem> {
		
		SortDirection sortDirection;
		
		public WebHookHistoryRepositoryDateSortingCompator(SortDirection sortDirection) {
			this.sortDirection = sortDirection;
		}
		
		@Override
		public int compare(WebHookHistoryItem o1, WebHookHistoryItem o2) {
			
			if (sortDirection == SortDirection.DESC) {
				if (o1.getTimestamp().isBefore(o2.getTimestamp())){
					return 1;
				} else if (o1.getTimestamp().isAfter(o2.getTimestamp())){
					return -1;
				} else {
					return 0;
				}
			} else {
				if (o1.getTimestamp().isAfter(o2.getTimestamp())){
					return 1;
				} else if (o1.getTimestamp().isBefore(o2.getTimestamp())){
					return -1;
				} else {
					return 0;
				}				
			}
		}
	}
	
	public enum SortDirection {
		ASC,
		DESC;
	}

}
