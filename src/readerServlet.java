import org.json.JSONException;
import org.json.JSONObject;
import util.DB;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "readerServlet")
public class readerServlet extends HttpServlet {
    Map<String, String> attributes;
    DB db;
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println(request.getRequestURI());
        System.out.println(request.getParameter("bname"));
        doGet(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("执行servlet!");
        response.setContentType("text/html; charset=UTF-8");
        //request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");
        PrintWriter pw=response.getWriter();
        attributes = new HashMap<String, String>();
        db = new DB();
        //System.out.println(request);
        int active =1;
        String temp=request.getParameter("active");
        if(temp!=null&&temp!="")
            active=Integer.parseInt(temp);
        int length=10;
        temp=request.getParameter("length");
        if(temp!=null&&temp!="")
            length=Integer.parseInt(temp);
        int pagenum=1;
        String uname = request.getParameter("uname");
        if (uname != null&&uname!="")
            attributes.put("uname", uname);
        String ugender = request.getParameter("ugender");
        if (ugender != null&&ugender!="")
            attributes.put("ugender", ugender);
        String udepart = request.getParameter("udepart");
        if (udepart != null&&udepart!="")
            attributes.put("udepart", udepart);
        String ugrade = request.getParameter("ugrade");
        if (ugrade != null&&ugrade!="")
            attributes.put("ugrade", ugrade);
        String ulicense = request.getParameter("license");
        if (ulicense != null&&ulicense!="")
            attributes.put("license", ulicense);
        String upassword = request.getParameter("upassword");
        if (upassword != null&&upassword!="")
            attributes.put("upassword", upassword);
        System.out.println(attributes);
        //System.out.println(request.getParameter("add"));
        //System.out.println(request.getParameter("query").equals("true"));
        if (request.getParameter("query")!=null){
            //System.out.println("querry");
            JSONObject result= doQuery(active,length);
            pw.print(result);

        }
        else if(request.getParameter("add")!=null){
            //System.out.println("adddd");
            int result=doAdd();
            response.setStatus(200);
            pw.print(result);
        }
        else if(request.getParameter("delete")!=null){
            String[] ids=request.getParameterValues("delete_lines[]");
            System.out.println(ids[0]);
            //System.out.println(ids[1]);
            int result=doDelete(ids);
            pw.print(result);
        }
        else if(request.getParameter("update")!=null){
            String id=request.getParameter("userid");
            int result=doUpdate(id);
            pw.print(result);
        }
        //System.out.println(request.getParameter("add").equals("true"));
        db.closestate();
        //response.setStatus(200);
    }

    protected JSONObject doQuery(int active,int length){
        List jsonlist=new ArrayList<JSONObject>();
        JSONObject result=new JSONObject();
        int pagenum=1;
        int size = attributes.size();
        ResultSet rs;
        ResultSet count;
        if (size == 0) {
            rs = db.query("SELECT * FROM reader ORDER BY userid desc limit "+ String.valueOf((active-1)*length)+","+String.valueOf(length));
            count=db.query("SELECT count(*) from reader");
            System.out.println("qqqqq");
        } else {
            String s = "SELECT * FROM reader where";
            String s1="SELECT count(*) FROM reader where";
            int i = 0;
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                i++;
                if (i < size) {
                    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                    s = s + " " + entry.getKey() + "='" + entry.getValue() + "' AND";
                    s1 = s1 + " " + entry.getKey() + "='" + entry.getValue() + "' AND";
                }
                else {
                    s = s + " " + entry.getKey() + "='" + entry.getValue() + "'";
                    s1 = s1 + " " + entry.getKey() + "='" + entry.getValue() + "'";
                }
            }
            s=s+" ORDER BY bookid desc limit "+ String.valueOf((active-1)*length)+","+String.valueOf(length);
            //System.out.println(s);
            //System.out.println(s1);
            rs = db.query(s);
            count=db.query(s1);

        }

        try {
            if (rs != null) {
                while (rs.next()) {
                    JSONObject json = new JSONObject();
                    String id=rs.getString("userid");
                    json.put("userid",id);
                    String name = rs.getString("uname");
                    json.put("uname", name);
                    String aut = rs.getString("ugender");
                    json.put("ugender", aut);
                    String isb = rs.getString("udepart");
                    json.put("udepart", isb);
                    String cb = rs.getString("ugrade");
                    json.put("ugrade", cb);
                    String yea = rs.getString("license");
                    json.put("license", yea);
                    String per = rs.getString("permission");
                    json.put("permission", per);
                    jsonlist.add(json);
                }
                rs.close();
            }
            if(count!=null){
                while(count.next()){
                    pagenum=count.getInt(1);
                }
            }

            //PrintWriter pw = response.getWriter();
            String jsonstring=jsonlist.toString();
            result.put("data",jsonlist);
            //System.out.println(pagenum);
            result.put("page_num",Math.ceil((double)pagenum/length));
            return result;
            //pw.print(result);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException j) {
            j.printStackTrace();
        }
        return result;
    }

    protected int doAdd(){
        String s="INSERT INTO reader(uname,license,ugender,udepart,ugrade,upassword) VALUES('"+attributes.get("uname")+"','"
                +attributes.get("license")+"','"+attributes.get("ugender")+"','"+attributes.get("udepart")+"','"
                +attributes.get("ugrade")+"','"+attributes.get("upassword")+"')";
        System.out.println(s);
        int result=db.executeupdate(s);
        System.out.println(result);
        return result;
    }

    protected int doDelete(String[] ids){
        int k=0;
        int length=ids.length;
        for (int i=0;i<length;i++){
            String s="DELETE from reader where userid='"+ids[i]+"'";
            System.out.println(s);
            k=db.executeupdate(s);
        }
        return k;
    }
    protected int doUpdate(String id){
        String s="UPDATE reader SET";
        int i=0;
        int size=attributes.size();
        int k=0;
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            i++;
            if (i < size) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
                s = s + " " + entry.getKey() + "='" + entry.getValue() + "',";
            }
            else {
                s = s + " " + entry.getKey() + "='" + entry.getValue() + "'";
            }
        }
        s+=" where userid='"+id+"'";
        System.out.println(s);
        k=db.executeupdate(s);
        return k;
    }
}

