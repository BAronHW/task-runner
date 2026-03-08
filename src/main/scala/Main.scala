import java.io.File

object Main {

  def getListOfFilesFromDir(dir: String): List[String] = {
    val file = new File(dir)
    file.listFiles.filter(_.isFile)
      .filter(_.getName.endsWith(".json"))
      .map(_.getPath).toList
  }

  def main(args: Array[String]) = {
    // need to read all directories recursively then look for json or whatever file format
    val currentPath = System.getProperty("user.dir")
    val listOfFiles = getListOfFilesFromDir(currentPath)
    println(listOfFiles)
  }

}
