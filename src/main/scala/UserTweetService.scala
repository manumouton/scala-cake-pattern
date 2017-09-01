import scala.collection.mutable

case class User(id: String, firstName: String, lastName: String)

case class Tweet(id: String, userId: String, content: String)

//What a user Repository should do
trait UserRepository {
  def createUser(user: User): Option[User]

  def getUser(id: String): Option[User]
}

//A trait to expose the user repository
trait UserRepositoryComponent {
  val userRepository: UserRepository
}

//A default implementation of our User repository component
trait InMemoryUserRepositoryComponent extends UserRepositoryComponent {
  override val userRepository: UserRepository = new InMemoryUserRepository

  class InMemoryUserRepository extends UserRepository {
    val allUsers = new mutable.HashMap[String, User]

    override def createUser(user: User): Option[User] = allUsers.put(user.id, user)

    override def getUser(id: String): Option[User] = allUsers.get(id)
  }

}

trait TweetRepository {
  def createTweet(tweet: Tweet): Option[Tweet]

  def getTweet(id: String): Option[Tweet]

  def getAllByUser(user: User): List[Tweet]
}

trait TweetRepositoryComponent {
  val tweetRepository: TweetRepository
}

//A default implementation of our Tweet repository component
trait InMemoryTweetRepositoryComponent extends TweetRepositoryComponent {

  override val tweetRepository: TweetRepository = new InMemoryTweetRepository

  class InMemoryTweetRepository extends TweetRepository {
    val allTweets = new mutable.HashMap[String, Tweet]

    override def createTweet(tweet: Tweet): Option[Tweet] = allTweets.put(tweet.id, tweet)

    override def getTweet(id: String): Option[Tweet] = allTweets.get(id)

    override def getAllByUser(user: User): List[Tweet] = allTweets.values.filter(user.id == _.userId).toList
  }

}


trait UserTweetService {
  def createUser(user: User): Option[User]

  def createTweet(tweet: Tweet): Option[Tweet]

  def getUser(id: String): Option[User]

  def getTweet(id: String): Option[Tweet]

  def getUserAndTweets(id: String): (User, List[Tweet])
}

trait UserTweetServiceComponent {
  val userTweetService: UserTweetService
}


trait DefaultUserTweetServiceComponent extends UserTweetServiceComponent {
  self: UserRepositoryComponent with TweetRepositoryComponent =>

  override val userTweetService: UserTweetService = new DefaultUserTweetService

  class DefaultUserTweetService extends UserTweetService {
    override def createUser(user: User): Option[User] = userRepository.createUser(user)

    override def createTweet(tweet: Tweet): Option[Tweet] = tweetRepository.createTweet(tweet)

    override def getUser(id: String): Option[User] = userRepository.getUser(id)

    override def getTweet(id: String): Option[Tweet] = tweetRepository.getTweet(id)

    override def getUserAndTweets(id: String): (User, List[Tweet]) = {
      val user = userRepository.getUser(id)
      val tweets = tweetRepository.getAllByUser(user.get)
      (user.get, tweets)
    }
  }
}

trait MyApplication extends UserTweetServiceComponent

trait MyApplicationMixin
  extends MyApplication
    with DefaultUserTweetServiceComponent
    with InMemoryUserRepositoryComponent
    with InMemoryTweetRepositoryComponent

object Service {
  def main(args: Array[String]): Unit = {
    val app: MyApplication = new MyApplicationMixin {}

    val user1 = User("userId1", "Manu", "Mouton")
    val user2 = User("userId2", "Bart", "Simpson")
    app.userTweetService.createUser(user1)
    app.userTweetService.createUser(user2)
    println("userId1 = " + app.userTweetService.getUser("userId1"))
    println("userId2 = " + app.userTweetService.getUser("userId2"))
    println("userId3 = " + app.userTweetService.getUser("userId3"))
    println()

    println("get user1 with tweets = " + app.userTweetService.getUserAndTweets(user1.id))
    println("get user2 with tweets = " + app.userTweetService.getUserAndTweets(user2.id))
    println()

    app.userTweetService.createTweet(Tweet("tweetId1", user1.id, "Content tweet 1"))
    app.userTweetService.createTweet(Tweet("tweetId2", user1.id, "Content tweet 2"))
    app.userTweetService.createTweet(Tweet("tweetId3", user1.id, "Content tweet 3"))
    app.userTweetService.createTweet(Tweet("tweetId4", user2.id, "Content tweet 4"))
    println("get user1 with tweets after insert = " + app.userTweetService.getUserAndTweets(user1.id))
    println("get user2 with tweets after insert = " + app.userTweetService.getUserAndTweets(user2.id))
  }
}


