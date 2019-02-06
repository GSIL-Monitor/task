package com.fymod.task;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
public class DomainTask {
	
	@Autowired private JavaMailSender mailSender; 
	
	@Value("${custom.domain}")
	private String domain;

	@Scheduled(cron = "0 0 9 * * ?")
//	@Scheduled(fixedRate = 50000) //测试使用，5秒一次
	public void domainTaskCronTrigger() throws IOException, InterruptedException {
		String[] domains = domain.split(",");
		for(String d : domains) {
			Thread.sleep(2000);
			Document doc = Jsoup.connect("http://whois.chinaz.com/" + d).timeout(30000).get();
			Element ul = doc.getElementsByClass("WhoisLeft fl").first();
			if(ul == null) {
				SimpleMailMessage message = new SimpleMailMessage();
			    message.setFrom("send@zhaoguojian.com");
			    message.setTo("me@zhaoguojian.com");
			    message.setSubject("您关注的域名可注册");
			    message.setText("您关注的域名可注册：" + d);
			    mailSender.send(message);
			    return;
			}
			Elements lis = ul.children();
			String all = "";
			String limitDate = null;
			for(int i = 0; i < lis.size()-2; i++) {
				Element li = lis.get(i);
				String infomation = li.text().replaceAll("\\[whois 反查\\] 申请删除隐私 其他常用域名后缀查询： cn com cc net org", "").replaceAll("\\[whois反查\\]", "");
				all += infomation + "\n";
				if(infomation.contains("过期时间")) { //有过期时间的
					limitDate = infomation.split(" ")[1];
				}
			}
			if(limitDate != null && new SimpleDateFormat("yyyy年MM月dd日").format(new Date()).equals(limitDate)) {
				SimpleMailMessage message = new SimpleMailMessage();
			    message.setFrom("send@zhaoguojian.com");
			    message.setTo("me@zhaoguojian.com");
			    message.setSubject("您关注的域名即将到期" + limitDate);
			    message.setText(all);
			    mailSender.send(message);
			}
		}
	}
	
	
	
}
