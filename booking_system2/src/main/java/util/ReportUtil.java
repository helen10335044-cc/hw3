package util;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import service.ProductService;
import service.OrderService;
import vo.Page;
import model.Product;
import model.Order;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportUtil {

    public static String exportProductReportToPdf() throws Exception {
        ProductService ps = new ProductService();
        List<Product> list = ps.listAll();

        JasperReport report = JasperCompileManager.compileReport(
                ReportUtil.class.getResourceAsStream("/report/products.jrxml"));

        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(list);

        Map<String, Object> params = new HashMap<>();
        params.put("title", "Product Report");

        JasperPrint jp = JasperFillManager.fillReport(report, params, ds);

        File outDir = new File("report-output");
        if (!outDir.exists()) outDir.mkdirs();

        String outFile = "report-output/products.pdf";
        JasperExportManager.exportReportToPdfFile(jp, outFile);
        return new File(outFile).getAbsolutePath();
    }

    public static String exportOrderReportToPdf() throws Exception {
        OrderService os = new OrderService();
        Page<Order> page = os.listOrders(null, null, null, null, 1, 200);
        List<Order> list = page.getItems();

        JasperReport report = JasperCompileManager.compileReport(
                ReportUtil.class.getResourceAsStream("/report/orders.jrxml"));

        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(list);

        Map<String, Object> params = new HashMap<>();
        params.put("title", "Order Report");

        JasperPrint jp = JasperFillManager.fillReport(report, params, ds);

        File outDir = new File("report-output");
        if (!outDir.exists()) outDir.mkdirs();

        String outFile = "report-output/orders.pdf";
        JasperExportManager.exportReportToPdfFile(jp, outFile);
        return new File(outFile).getAbsolutePath();
    }
}
