package vo;

import java.util.List;

public class Page<T> {
    private final List<T> items;
    private final int page;      // 1-based
    private final int pageSize;
    private final long total;

    public Page(List<T> items, int page, int pageSize, long total) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
    }

    public List<T> getItems() { return items; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public long getTotal() { return total; }

    public long getTotalPages() {
        return (long) Math.ceil(total * 1.0 / pageSize);
    }
}
