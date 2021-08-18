import com.amazonaws.services.glue.util.GlueArgParser  
import com.amazonaws.services.glue.util.Job  
import com.amazonaws.services.glue.util.JsonOptions  
import com.amazonaws.services.glue.{DynamicFrame, GlueContext}  
import org.apache.spark.SparkContext  
import scala.collection.JavaConverters.mapAsJavaMapConverter  
  
object SalesforceToS3ExtractData {  
  def main(sysArgs: Array[String]) {  
      
    val sparkContext: SparkContext = new SparkContext()  
    val glueContext: GlueContext = new GlueContext(sparkContext)  
    val sparkSession = glueContext.getSparkSession  
    
    val args = GlueArgParser.getResolvedOptions(sysArgs, Seq("JOB_NAME").toArray)  
    Job.init(args("JOB_NAME"), glueContext, args.asJava)  
    
    println("Connecting salesforce...")
    val soql = "select name, accountnumber, industry, type, billingaddress, sic from account"  
    val df = sparkSession.read.format("com.springml.spark.salesforce").option("soql",soql).option("username", "shgupta94centelon@gmail.com").option("password","<password><authentication_token>").load()
    
    
    val datasource0 = DynamicFrame(df, glueContext).withName("datasource0").withTransformationContext("datasource0")  
        
    println("Loading s3...")
    val datasink1 = glueContext.getSinkWithFormat(connectionType = "s3", options = JsonOptions(Map("path" -> "s3://redshift-demo-123456/sfdc-output", "partitionKeys" -> Seq("Industry"))), format = "parquet", transformationContext = "datasink1").writeDynamicFrame(datasource0)  
  
    Job.commit()  
    
    println("Success")
  }  
}