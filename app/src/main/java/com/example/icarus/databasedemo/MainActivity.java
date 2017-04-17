package com.example.icarus.databasedemo;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btn1,btn2,btn3,btn4,btn5,btn6,btn7,btn8,btn9,btn10;
    private TextView textView;
    MySQLiteOpenHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.main_tv_data);
        //第一个参数是Context，第二个参数是数据库名，第三个参数是一个自定义的Cursor，一般填null就可以，第四个参数是数据库版本号，用于判断数据库是否需要更新
        //当数据库不存在时，OpenHelper就会自动创建数据库，并执行onCreate()方法，如果数据库已经有了，就不会再执行onCreate()方法了
        //当版本号变更后，OpenHelper就会调用onUpgrade()方法，这样可以完成数据库的升级
        helper = new MySQLiteOpenHelper(MainActivity.this,"people.db",null,1);

        //保存数据到文件中
        btn1 = (Button) findViewById(R.id.main_btn_savetofile);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToFile();
                loadDataFromFile();
            }
        });
        //保存数据到SharedPreferences
        btn2 = (Button) findViewById(R.id.main_btn_SharedPreferences);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToSharedPreferences();
                loadDataFromSharedPreferences();
            }
        });

        //SQLiteDatabase操作数据库
        btn3 = (Button) findViewById(R.id.main_btn_save);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToDataBase();
            }
        });

        btn4 = (Button) findViewById(R.id.main_btn_load);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDataFromDataBase();
            }
        });

        btn5 = (Button) findViewById(R.id.main_btn_update);
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDataBase();
            }
        });

        btn6 = (Button) findViewById(R.id.main_btn_delet);
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletDataBase();
            }
        });


        //LitePal操作数据库
        //调用getDatabase()方法就可以创建一个数据库，表按照配置的模型类自动生成
        //如果要升级数据库，只要修改模型类的内容，或添加一个新的模型类(新的模型类也要在litepal.xml里配置)，
        //然后修改litepal.xml里的版本号，就会自动升级数据库，并且原来的数据不会丢失
        LitePal.getDatabase();

        btn7 = (Button) findViewById(R.id.main_btn_LitePal_save);
        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToLitePal();
            }
        });

        btn8 = (Button) findViewById(R.id.main_btn_LitePal_load);
        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDataFromLitePal();
            }
        });

        btn9 = (Button) findViewById(R.id.main_btn_LitePal_update);
        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDataFromLitePal();
            }
        });

        btn10 = (Button) findViewById(R.id.main_btn_LitePal_delete);
        btn10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDataFromLitePal();
            }
        });
    }

    private void loadDataFromLitePal() {
        List<Book> books = DataSupport.findAll(Book.class);
        StringBuffer stringBuffer = new StringBuffer();
        for(Book book:books){
            stringBuffer.append(book.getName()+" "+book.getAuthor()+" "+book.getPages()+" "+book.getPrice());
        }
        textView.setText(stringBuffer);
        //查询特定的字段
        //List<Book> books = DataSupport.select("name","author").find(Book.class);
        //添加查询约束
        //List<Book> books = DataSupport.where("pages>?","400").find(Book.class);
        //对查询结果进行排序，desc表示降序，asc或不写表示升序
        //List<Book> books = DataSupport.order("price desc").find(Book.class);
        //表示只查询前面几条数据
        //List<Book> books = DataSupport.limit(3).find(Book.class);
        //表示查询结果的偏移量，如这里查询的是第二，第三，第四条数据
        //List<Book> books = DataSupport.limit(3).offset(1).find(Book.class);
        //也可以将这几种方法组合起来用，如
        //List<Book> books = DataSupport.select("name").where("pages>?","100").limit(5).find(Book.class);
        //还可以使用原生的SQL语句,返回的是一个Cursor对象，所以还要一个一个的将数据去出来
        //Cursor c = DataSupport.findBySQL("select * from Book where pages > ? and price < ?","400","20")
    }

    private void deleteDataFromLitePal() {
        //第一种是直接用delete()方法来删除，对象可以是调用过save()方法的，也可以是查询方法得到的
        //第二种是调用DataSupport.deleteAll()方法来删除，第一个参数指定删除哪张表，传入一个class对象，
        //第二个参数是一个约束条件，第三个参数用来指定约束条件中占位符的具体值
        DataSupport.deleteAll(Book.class,"price > ?","50");
    }

    private void updateDataFromLitePal() {
        Book book = new Book();
        book.setName("The Lost Symbol");
        book.setAuthor("Dan Brown");
        book.setPages(454);
        book.setPrice(16.96);
        book.save();
        //LitePal可以直接使用save()方法对一个已储存的对象进行更新，不过这种方式限制比较大
//        book.setPrice(10.99);
//        book.save();
        Book newbook = new Book();
        newbook.setPrice(99.99);
        newbook.setToDefault("pages");
        //这里可以指定一个约束条件，后面每个具体的值与条件中的占位符一一对应，如果不指定条件就默认修改所有行
        //使用updateAll方法时必须要知道，如果要更新一个字段的值为默认值时，是不可以直接设置的，可以使用LitePal提供的setToDefault()方法
        newbook.updateAll("name = ? and author = ?","The Lost Symbol","Dan Brown");
    }

    private void saveDataToLitePal() {
        Book book = new Book();
        book.setName("The Da Vinci Code");
        book.setAuthor("Dan Brown");
        book.setPages(454);
        book.setPrice(16.96);
        book.save();

    }

    private void deletDataBase(){
        SQLiteDatabase db = helper.getWritableDatabase();
        //如果第三和第四个参数不指定，就默认删除所有行数据
        db.delete("people","name = ?",new String[]{"lisi"});
    }

    private void loadDataFromDataBase() {
        SQLiteDatabase db = helper.getWritableDatabase();
        //最短的也要七个参数，第一个指定查询的表名，第二个指定查询的列名，第三个指定where的约束条件，第四个为where中的占位符提供具体的值
        //第五个指定需要group by的列，第六个对group by后的结果进一步约束，第七个指定查询结果的排序方式
        Cursor cursor = db.query("people",null,null,null,null,null,null);
        StringBuffer data = new StringBuffer();
        if(cursor.moveToFirst()){
            do{
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                int age = cursor.getInt(cursor.getColumnIndex("age"));
                data.append("id:+"+id+" name:"+name+" age:"+age+"  ");
            }while (cursor.moveToNext());
        }
        cursor.close();
        textView.setText(data.toString());
    }

    private void updateDataBase(){
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name","lisi");
        //第三个参数相当于SQL语句里的where，第四个参数提供的一个字符串数组为第三个参数中的每个占位符指定相应内容
        db.update("people",values,"name = ?",new String[]{"zhangsan"});
    }

    private void saveDataToDataBase() {

        //getWritableDatabase()和getReadableDatabase()的区别是 当内存已满时，getReadable会返回一个只读的对象，而getWritable则会报错
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name","zhangsan");
        values.put("age",18);
        //第一个参数是表名，第二个参数用于再为指定添加数据的情况下给某些可为空的列自动赋值null，第三个参数是一个ContentValues对象
        db.insert("people",null,values);
    }

    private void loadDataFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("data",MODE_PRIVATE);
        //第一个参数是储存的数据名，第二个参数是该数据不存在的时候返回的值
        String name = sharedPreferences.getString("name","");
        int age = sharedPreferences.getInt("age",0);
        textView.setText("name:"+name+"  age:"+age);
    }

    private void saveDataToSharedPreferences() {
        //一共有三种获取SharedPreferences实例的方式，调用SharedPreferences对象的edit()方法获取SharedPreferences.Editor对象
        //调用putXXX()(ps:XXX表示数据类型)方法向SharedPreferences.Editor中添加数据，数据以键值对的方式写入
        // 然后调用apply()方法将数据提交

        //一、Context类中的getSharedPerences()方法，第一个参数是文件名，文件保存在/data/data/<packge name>/shared_prefs目录下
        //第二个参数是操作模式，除MODE_PRIVATE外的都被废除了，和直接传入0效果是相同的，表示只有本应用程序才能对这个文件读写
        SharedPreferences.Editor editor1 = getSharedPreferences("data",MODE_PRIVATE).edit();

        //二、Activity类中的getPreferences()方法，只有一个操作模式的参数，使用这个方法，会自动以当前类名作为文件名
        SharedPreferences.Editor editor2 = getPreferences(0).edit();

        //三、PreferenceManager类中的getDefaultSharedPreferences()方法，这是一个静态方法，接收一个Context参数
        //自动使用当前应用的包名作为文件名
        SharedPreferences.Editor editor3 = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();

        editor1.putString("name","Tom");
        editor1.putInt("age",16);
        editor1.apply();
    }


    private void loadDataFromFile() {
        FileInputStream fileInputStream = null;
        BufferedReader bufferedReader = null;
        StringBuilder content = new StringBuilder();

        try {
            //参数是文件名
            // StringBuilder与StringBuffered几乎一致，但是在单线程中，StringBuilder比StringBuffered要更好一些
            //

            fileInputStream = openFileInput("data");
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line = "";
            while((line = bufferedReader.readLine()) != null){
                content.append(line);
            }
            textView.setText(content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDataToFile() {
        String data = "data to save";
        FileOutputStream outputStream = null;
        BufferedWriter bufferedWriter = null;

        try {
            //第一个参数是文件名，第二个参数是操作模式，MODE_PRIVATE是默认的操作模式，表示当制定同样文件名的时候，
            //所写入的内容将会覆盖原文件的内容，MODE_APPEND表示如果该文件已存在，则在文件里追加内容，不存在就创建新文件
            outputStream = openFileOutput("data", Context.MODE_PRIVATE);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            bufferedWriter.write(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(bufferedWriter != null){
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



}
