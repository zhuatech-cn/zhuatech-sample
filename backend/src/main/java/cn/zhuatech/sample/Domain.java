/* 上海如静知华信息科技有限公司 https://www.zhuatech.cn/ */
package cn.zhuatech.sample;
import org.springframework.stereotype.Component;
import java.util.*;
import java.math.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import static cn.zhuatech.sample.Model.*;
import static cn.zhuatech.sample.Engine.*;
/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@Component public class Domain {
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static Map<String,Object> copy(Row r){return new LinkedHashMap<>(r.data());}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static BigDecimal n(Row r,String k){return num(r.data(),k);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static BigDecimal z(Map<String,Object>d,String k){return d.containsKey(k)?num(d,k):BigDecimal.ZERO;}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static String t(Row r,String k){return txt(r.data(),k);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static List<Row> linked(Engine e,User u,String module,String key,String id){return e.all(u,module).stream().filter(r->t(r,key).equals(id)).toList();}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void unique(Engine e,User u,String module,Map<String,Object>d,String key){require(e.all(u,module).stream().noneMatch(r->t(r,key).equalsIgnoreCase(txt(d,key))),"重复的"+key);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void dates(Map<String,Object>d,String from,String to){require(!date(d,to).isBefore(date(d,from)),"结束日期不能早于开始日期");}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 static void change(Engine e,User u,Row row,String state,Map<String,Object>d,String note){e.save(u,row,state,d,"LINKED",note);}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void edit(Engine e,User u,Row r,Map<String,Object>d){
  if(r.module().equals("readings")){require(e.ref(u,r.data(),"job","jobs").state().equals("RUNNING")&&txt(d,"job").equals(t(r,"job")),"仅进行中的任务可以修改测量值，且不得迁移任务");require(linked(e,u,"readings","job",t(r,"job")).stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"point").equals(txt(d,"point"))),"测量点编号重复");return;}
  if(r.module().equals("versions")){require(e.ref(u,r.data(),"artwork","artworks").state().equals("DRAFT"),"已送审稿件不可修改");require(txt(d,"artwork").equals(t(r,"artwork"))&&num(d,"revision").compareTo(n(r,"revision"))==0,"版本不能迁移任务或改写版本号");return;}
  for(var m:e.spec().modules())for(Row other:e.all(u,m.key()))if(!other.id().equals(r.id())&&other.data().values().stream().anyMatch(v->r.id().equals(v)))throw new Failure(409,"资料已有下游引用，请新建版本而不是改写历史");
  var fields=e.spec().module(r.module()).fields().stream().map(Field::key).toList();
  r.data().forEach((k,v)->{if(!fields.contains(k))d.put(k,v);});
  if(d.containsKey("start")&&d.containsKey("end"))dates(d,"start","end");
  if(d.containsKey("from")&&d.containsKey("to"))dates(d,"from","to");
  for(String key:List.of("serial","sku","invoice","invoiceNo","lockNo"))if(d.containsKey(key))require(e.all(u,r.module()).stream().noneMatch(x->!x.id().equals(r.id())&&t(x,key).equalsIgnoreCase(txt(d,key))),"重复唯一业务标识: "+key);
  if(d.containsKey("bonusRate"))require(num(d,"bonusRate").compareTo(num(d,"baseRate"))>=0,"达档返利率不能低于基础返利率");
  if(d.containsKey("lifeLimit"))require(num(d,"serviceEvery").compareTo(num(d,"lifeLimit"))<=0,"保养间隔不能大于寿命");
  if(d.containsKey("defects"))require(num(d,"defects").compareTo(num(d,"shots"))<=0,"不良数不能超过生产次数");
  if(d.containsKey("nps"))require(num(d,"nps").compareTo(BigDecimal.TEN)<=0&&num(d,"csat").compareTo(new BigDecimal("5"))<=0,"评价分数超出范围");
  if(d.containsKey("oxygenMin"))require(num(d,"oxygenMin").compareTo(num(d,"oxygenMax"))<0,"氧气下限须小于上限");
  if(r.module().equals("invoices"))require(e.all(u,"invoices").stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"shipment").equals(txt(d,"shipment"))),"运单已关联结算账单");
  if(r.module().equals("sales")){Row program=e.ref(u,d,"program","programs");require(program.state().equals("ACTIVE")&&!date(d,"soldAt").isBefore(date(program.data(),"start"))&&!date(d,"soldAt").isAfter(date(program.data(),"end")),"协议状态或销售日期无效");}
  if(r.module().equals("jobs")){Row instrument=e.ref(u,d,"instrument","instruments"),standard=e.ref(u,d,"standard","standards");require(!instrument.state().equals("RETIRED")&&t(instrument,"unit").equals(t(standard,"unit")),"器具状态或计量单位无效");require(!date(d,"performedAt").isAfter(LocalDate.now()),"不能记录未来校准");}
  if(r.module().equals("permits")){require(ChronoUnit.DAYS.between(date(d,"start"),date(d,"end"))<=7,"许可最长七天");require(t(e.ref(u,d,"isolation","isolations"),"location").equals(txt(d,"location")),"隔离区域不匹配");}
  if(r.module().equals("responses")){require(e.all(u,"responses").stream().noneMatch(x->!x.id().equals(r.id())&&t(x,"survey").equals(txt(d,"survey"))&&t(x,"customer").equals(txt(d,"customer"))),"客户已存在该问卷反馈");require(t(e.ref(u,d,"customer","customers"),"consent").equals("YES"),"客户未允许反馈邀请");}
  if(r.module().equals("products")){String barcode=txt(d,"barcode");require(barcode.matches("\\d{13}"),"条码须为 EAN-13");int sum=0;for(int x=0;x<12;x++)sum+=(barcode.charAt(x)-'0')*(x%2==0?1:3);require((10-sum%10)%10==barcode.charAt(12)-'0',"EAN-13 校验位不正确");}

 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public Map<String,Object> metrics(Engine e,User u){
  var out=new LinkedHashMap<String,Object>();out.put("可借样品",e.all(u,"samples").stream().map(r->n(r,"onHand").subtract(n(r,"reserved"))).reduce(BigDecimal.ZERO,BigDecimal::add));out.put("超期借样",e.all(u,"requests").stream().filter(r->r.state().equals("OUT")&&date(r.data(),"dueDate").isBefore(LocalDate.now())&&n(r,"returned").compareTo(n(r,"quantity"))<0).count());out.put("损坏待处置",e.all(u,"samples").stream().map(r->n(r,"damaged")).reduce(BigDecimal.ZERO,BigDecimal::add));;return out;
 }
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public void create(Engine e,User u,String module,Map<String,Object>d){switch(module){case "samples" -> {unique(e,u,module,d,"sku");d.put("reserved",0);d.put("out",0);d.put("damaged",0);d.put("scrapped",0);}
case "requests" -> {require(!date(d,"dueDate").isBefore(LocalDate.now()),"申请归还期限不能早于今天");d.put("returned",0);} default -> {} }}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 public String action(Engine e,User u,Row r,String action,Map<String,Object>i,Map<String,Object>d){
  String k=r.module()+"."+action;switch(k){
case "requests.approve" -> {
 Row sample=e.ref(u,d,"sample","samples");require(z(d,"quantity").compareTo(n(sample,"onHand").subtract(n(sample,"reserved")))<=0,"可借库存不足");var sd=copy(sample);sd.put("reserved",n(sample,"reserved").add(z(d,"quantity")));change(e,u,sample,sample.state(),sd,"借样预留");e.ledger(u,"movements","POSTED",Map.of("sample",sample.id(),"request",r.id(),"kind","RESERVE","quantity",z(d,"quantity")));
}
case "requests.ship" -> {
 Row sample=e.ref(u,d,"sample","samples");var sd=copy(sample);sd.put("reserved",n(sample,"reserved").subtract(z(d,"quantity")));sd.put("onHand",n(sample,"onHand").subtract(z(d,"quantity")));sd.put("out",n(sample,"out").add(z(d,"quantity")));change(e,u,sample,sample.state(),sd,"样品发出");d.putAll(i);e.ledger(u,"movements","POSTED",Map.of("sample",sample.id(),"request",r.id(),"kind","SHIP","quantity",z(d,"quantity")));
}
case "requests.extend" -> {require(date(i,"dueDate").isAfter(date(d,"dueDate")),"新期限须晚于原期限");d.putAll(i);}
case "requests.cancel" -> {if(r.state().equals("RESERVED")){Row sample=e.ref(u,d,"sample","samples");var sd=copy(sample);sd.put("reserved",n(sample,"reserved").subtract(z(d,"quantity")));change(e,u,sample,sample.state(),sd,"撤销释放预留");e.ledger(u,"movements","POSTED",Map.of("sample",sample.id(),"request",r.id(),"kind","RELEASE","quantity",z(d,"quantity")));}}
case "returns.receive" -> {
 Row request=e.ref(u,d,"request","requests");require(request.state().equals("OUT"),"借样单不在借出状态");BigDecimal total=z(d,"good").add(z(d,"damaged"));require(total.signum()>0&&total.compareTo(n(request,"quantity").subtract(n(request,"returned")))<=0,"归还数量为零或超过未归还量");
 Row sample=e.ref(u,request.data(),"sample","samples");var sd=copy(sample);sd.put("onHand",n(sample,"onHand").add(z(d,"good")));sd.put("out",n(sample,"out").subtract(total));sd.put("damaged",n(sample,"damaged").add(z(d,"damaged")));change(e,u,sample,sample.state(),sd,"样品验收归还");
 var rd=copy(request);rd.put("returned",n(request,"returned").add(total));change(e,u,request,request.state(),rd,"累计样品归还量");e.ledger(u,"movements","POSTED",Map.of("sample",sample.id(),"request",request.id(),"kind","RETURN","quantity",total,"good",z(d,"good"),"damaged",z(d,"damaged")));
}
case "requests.close" -> require(z(d,"returned").compareTo(z(d,"quantity"))==0,"仍有未归还样品");
case "disposals.approve" -> {
 Row sample=e.ref(u,d,"sample","samples");BigDecimal qty=z(d,"quantity");require(qty.compareTo(n(sample,"damaged"))<=0,"处置超过损坏库存");var sd=copy(sample);sd.put("damaged",n(sample,"damaged").subtract(qty));String field=txt(d,"kind").equals("REPAIR")?"onHand":"scrapped";sd.put(field,n(sample,field).add(qty));change(e,u,sample,sample.state(),sd,"损坏样品处置");e.ledger(u,"movements","POSTED",Map.of("sample",sample.id(),"kind",txt(d,"kind"),"quantity",qty,"disposal",r.id()));
}

case "samples.restock" -> {require(e.all(u,"movements").stream().noneMatch(x->t(x,"reference").equals(txt(i,"reference"))),"入库凭证重复");BigDecimal qty=num(i,"quantity");d.put("onHand",z(d,"onHand").add(qty));e.ledger(u,"movements","POSTED",Map.of("sample",r.id(),"quantity",qty,"kind","RESTOCK","reference",txt(i,"reference")));} default -> {} }return null;
 }
}
