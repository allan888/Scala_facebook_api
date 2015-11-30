package com.xueliu

import java.util.Calendar

import akka.util.Timeout

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.util.{Random, Success, Failure}
import scala.concurrent.duration._
import akka.actor.{Props, Actor, ActorSystem}
import akka.pattern.ask
import akka.event.Logging
import akka.io.IO
import spray.json.{JsonFormat, DefaultJsonProtocol}
import spray.can.Http
import spray.httpx.SprayJsonSupport
import spray.client.pipelining._
import spray.util._
import akka.actor.PoisonPill

class Register extends Actor {


  import SprayJsonSupport._
  import IDJsonProtocol._
  import UserPassJsonProtocol._
  import TokenAndIdJsonProtocol._
  import OKJsonProtocol._
  import PersonInfoJsonProtocol._
  import TokenAndPersonInfoJsonProtocol._
  import scala.concurrent.ExecutionContext.Implicits.global
  val log = Logging(context.system, getClass)

  var reg_id = 1
  var suc = 0
  var fail = 0
  var done = 0




  def register() = {
    println("register",reg_id)
    val new_id = reg_id
    reg_id += 1
    val pipeline = sendReceive ~> unmarshal[ID]

    val responseFuture = pipeline {
      Post("http://127.0.0.1:8080/register/", UserPass("username"+new_id.toString,"123456"))
    }
    responseFuture onComplete {
      case Success(ID(id)) =>
        done += 1
        //log.info("registration succeed, user id:"+id.toString)

        val pipeline1 = sendReceive ~> unmarshal[TokenAndId]

        val responseFuture1 = pipeline1 {
          Post("http://127.0.0.1:8080/getToken/", UserPass("username"+id.toString,"123456"))
        }
        responseFuture1 onComplete {
          case Success(TokenAndId(t,i)) => {
            val pipeline2 = sendReceive ~> unmarshal[OK]

            val responseFuture2 = pipeline2 {
              Post("http://127.0.0.1:8080/me/", TokenAndPersonInfo(t,PersonInfo("username"+id.toString,Calendar.getInstance.getTime.toString, "male","Gainesville","Gainesville")))
            }
            responseFuture2 onComplete {
              case Success(OK(msg)) => {
                suc += 1
                ("reg",true)
              }
              case _ => {
                fail += 1
                ("reg",false)
              }
            }
          }
          case _ => {
            fail += 1
            ("reg",false)
          }
        }

      case Failure(error) =>
        fail += 1
        //log.error("registration failed")
        ("reg",false)

      case _ =>
        fail += 1
        //log.error("registration failed")
        ("reg",false)
    }
  }

  def receive = {
    case "reg" => sender ! register()
    case "reg_id" => sender ! done
  }
}

class SimulatedPerson(id:Int, times:Int) extends Actor {
  println("bunch",times)
  

  val userNumber = 100000 // have to be consistent with the number in object Main
  val maxConcurrency = 100

  //val postNum_FirstBunch = 10
  //val photoNum_FirstBunch = 1
  //val addFriendNum = 10
  //val postNum_SecondBunch = 30
  //val photoNum_SecondBunch = 3
  //val checkTimelineNum = 100
  //val visitOtherPersonNum = 10
  //val checkOtherProfileNum = 10
  //val checkOwnProfileNum = 10
  //var updateProfileNum = 0

  val postNum_FirstBunch = 1

  // if 4% user upload photos
  var photoNum_FirstBunch = Random.nextInt(10000)+1 // [1,10000]
  if(photoNum_FirstBunch > 9600){
    photoNum_FirstBunch = 2
  }else{
    photoNum_FirstBunch = 1
  }

  // if 1.8% user add friends
  var addFriendNum = Random.nextInt(10000)+1 // [1,10000]
  if(addFriendNum > 9820){
    addFriendNum = 2
  }else{
    addFriendNum = 1
  }

  // if 5% user upload posts
  var postNum_SecondBunch = Random.nextInt(10000)+1 // [1,10000]
  if(postNum_SecondBunch > 9500){
    postNum_SecondBunch = 2
  }else{
    postNum_SecondBunch = 1
  }

  // if 3% user upload photos
  var photoNum_SecondBunch = Random.nextInt(10000)+1 // [1,10000]
  if(photoNum_SecondBunch > 9700){
    photoNum_SecondBunch = 2
  }else{
    photoNum_SecondBunch = 1
  }

  // if 54% user check page (self+friend)
  var checkTimelineNum = Random.nextInt(10000)+1 // [1,10000]
  if(checkTimelineNum > 4600){
    checkTimelineNum = 2
  }else{
    checkTimelineNum = 1
  }

 // if 10% user check others page
  var visitOtherPersonNum = Random.nextInt(10000)+1 // [1,10000]
  if(visitOtherPersonNum > 9000){
    visitOtherPersonNum = 1
  }else{
    visitOtherPersonNum = 0
  }

 // if 8% user check others profile
  var checkOtherProfileNum = Random.nextInt(10000)+1 // [1,10000]
  if(checkOtherProfileNum > 9200){
    checkOtherProfileNum = 1
  }else{
    checkOtherProfileNum = 0
  }

 // if 1% user check own profile
  var checkOwnProfileNum = Random.nextInt(10000)+1 // [1,10000]
  if(checkOwnProfileNum > 9900){
    checkOwnProfileNum = 1
  }else{
    checkOwnProfileNum = 0
  }

  // if 0.4% user delete friends
  var deleteFriendNum = Random.nextInt(10000)+1 // [1,10000]
  if(deleteFriendNum > 9960){
    deleteFriendNum = 1
  } else{
    deleteFriendNum = 0
  }

  // if 0.4% user delete posts
  var deletePostNum = Random.nextInt(10000)+1 // [1,10000]
  if(deletePostNum > 9960){
    deletePostNum = 1
  }else{
    deletePostNum = 0
  }
 

   // if 1% user update profile
  var updateProfileNum = Random.nextInt(10000)+1 // [1,10000]
  if(updateProfileNum > 9900){
    updateProfileNum = 1
  }else{
    updateProfileNum = 0
  }


  val logActive = true

  import SprayJsonSupport._
  import IDJsonProtocol._
  import TokenAndIdJsonProtocol._
  import UserPassJsonProtocol._
  import MessageWithTokenJsonProtocol._
  import PhotoMessageWithTokenJsonProtocol._
  import OKJsonProtocol._
  import FriendIdWithTokenJsonProtocol._
  import TimelineNodeJsonProtocol._
  import PersonInfoJsonProtocol._
  import TokenAndPersonInfoJsonProtocol._

  import scala.concurrent.ExecutionContext.Implicits.global
  val log = Logging(context.system, getClass)
  var token = ""
  var myID = 0L
  val postList = new ListBuffer[Long]()
  val friendList = new ListBuffer[Long]()
  var friendNum = 0
  var deletionFinished = false
  var deletedFriendNum = 0
  var deletedPostNum = 0
  var postedNum = 0
  implicit val timeout = Timeout(240 seconds)


  val pipeline = sendReceive ~> unmarshal[TokenAndId]

  val responseFuture = pipeline {
    Post("http://127.0.0.1:8080/getToken/", UserPass("username"+id.toString,"123456"))
  }

  responseFuture onComplete {
    case Success(TokenAndId(t,i)) => {

      token = t
      myID = i
      if(logActive){
        log.info("getToken succeed, token:" + token)
      }
      // post 10 message
      for (i <- 0 until postNum_FirstBunch) {
        post()
      }
      // post 1 photo
      for (i <- 0 until photoNum_FirstBunch) {
        postPhoto()
      }
      // make 100 random friends
      for (i <- 0 until addFriendNum) {
        makeFriend()
      }
      // post 100 message
      for (i <- 0 until postNum_SecondBunch) {
        post()
      }
      // post 10 photo
      for (i <- 0 until photoNum_SecondBunch) {
        postPhoto()
      }
      // check timeline 100 times
      for (i <- 0 until checkTimelineNum) {
        checkTimeline()
      }
      // view other person's page 100 times
      for (i <- 0 until visitOtherPersonNum) {
        checkPage()
      }
      // check my profile 10 times
      for (i <- 0 until checkOwnProfileNum) {
        checkMyProfile()
      }
      // check other person's profiles 100 times
      for (i <- 0 until checkOtherProfileNum) {
        checkOtherProfile()
      }
      // update profile 10 times
      for (i <- 0 until updateProfileNum) {
        updateProfile()
      }

    }

    case Failure(error) =>
      println("fatal error")
      log.error("getToken failed"+error.toString)
      ("reg",false)

    case _ =>
      println("fatal error")
      log.error("getToken failed")
      ("reg",false)
  }

  def post() = {
    val message = Random.alphanumeric.take(60).mkString
    val pipeline = sendReceive ~> unmarshal[ID]
    val responseFuture = pipeline {
      Post("http://127.0.0.1:8080/"+myID.toString+"/feed/", MessageWithToken(message,token))
    }
    responseFuture onComplete {
      case Success(ID(id)) =>
        postList += id
        self ! "posted"
        if(logActive){
          log.info("post new message succeed, post id:"+id.toString)
        }

      case Failure(error) =>
        if(logActive){
          log.error("post new message failed")
        }

      case _ =>
        if(logActive){
          log.error("post new message failed")
        }
    }
  }

  def postPhoto() = {
    val message = Random.alphanumeric.take(60).mkString
    val pipeline = sendReceive ~> unmarshal[ID]
    val photoData = """/9j/4AAQSkZJRgABAQAAkACQAAD/4QB0RXhpZgAATU0AKgAAAAgABAEaAAUAAAABAAAAPgEbAAUAAAABAAAARgEoAAMAAAABAAIAAIdpAAQAAAABAAAATgAAAAAAAACQAAAAAQAAAJAAAAABAAKgAgAEAAAAAQAAAVygAwAEAAAAAQAAAEwAAAAA/+0AOFBob3Rvc2hvcCAzLjAAOEJJTQQEAAAAAAAAOEJJTQQlAAAAAAAQ1B2M2Y8AsgTpgAmY7PhCfv/iD0RJQ0NfUFJPRklMRQABAQAADzRhcHBsAhAAAG1udHJSR0IgWFlaIAffAAgAHwABADMAI2Fjc3BBUFBMAAAAAEFQUEwAAAAAAAAAAAAAAAAAAAAAAAD21gABAAAAANMtYXBwbAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEWRlc2MAAAFQAAAAYmRzY20AAAG0AAAEGmNwcnQAAAXQAAAAI3d0cHQAAAX0AAAAFHJYWVoAAAYIAAAAFGdYWVoAAAYcAAAAFGJYWVoAAAYwAAAAFHJUUkMAAAZEAAAIDGFhcmcAAA5QAAAAIHZjZ3QAAA5wAAAAMG5kaW4AAA6gAAAAPmNoYWQAAA7gAAAALG1tb2QAAA8MAAAAKGJUUkMAAAZEAAAIDGdUUkMAAAZEAAAIDGFhYmcAAA5QAAAAIGFhZ2cAAA5QAAAAIGRlc2MAAAAAAAAACERpc3BsYXkAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABtbHVjAAAAAAAAACIAAAAMaHJIUgAAABQAAAGoa29LUgAAAAwAAAG8bmJOTwAAABIAAAHIaWQAAAAAABIAAAHaaHVIVQAAABQAAAHsY3NDWgAAABYAAAIAZGFESwAAABwAAAIWdWtVQQAAABwAAAIyYXIAAAAAABQAAAJOaXRJVAAAABQAAAJicm9STwAAABIAAAJ2bmxOTAAAABYAAAKIaGVJTAAAABYAAAKeZXNFUwAAABIAAAJ2ZmlGSQAAABAAAAK0emhUVwAAAAwAAALEdmlWTgAAAA4AAALQc2tTSwAAABYAAALeemhDTgAAAAwAAALEcnVSVQAAACQAAAL0ZnJGUgAAABYAAAMYbXMAAAAAABIAAAMuY2FFUwAAABgAAANAdGhUSAAAAAwAAANYZXNYTAAAABIAAAJ2ZGVERQAAABAAAANkZW5VUwAAABIAAAN0cHRCUgAAABgAAAOGcGxQTAAAABIAAAOeZWxHUgAAACIAAAOwc3ZTRQAAABAAAAPSdHJUUgAAABQAAAPiamFKUAAAAA4AAAP2cHRQVAAAABYAAAQEAEwAQwBEACAAdQAgAGIAbwBqAGnO7LfsACAATABDAEQARgBhAHIAZwBlAC0ATABDAEQATABDAEQAIABXAGEAcgBuAGEAUwB6AO0AbgBlAHMAIABMAEMARABCAGEAcgBlAHYAbgD9ACAATABDAEQATABDAEQALQBmAGEAcgB2AGUAcwBrAOYAcgBtBBoEPgQ7BEwEPgRABD4EMgQ4BDkAIABMAEMARCAPAEwAQwBEACAGRQZEBkgGRgYpAEwAQwBEACAAYwBvAGwAbwByAGkATABDAEQAIABjAG8AbABvAHIASwBsAGUAdQByAGUAbgAtAEwAQwBEIA8ATABDAEQAIAXmBdEF4gXVBeAF2QBWAOQAcgBpAC0ATABDAERfaYJyACAATABDAEQATABDAEQAIABNAOAAdQBGAGEAcgBlAGIAbgDpACAATABDAEQEJgQyBDUEQgQ9BD4EOQAgBBYEGgAtBDQEOARBBD8EOwQ1BDkATABDAEQAIABjAG8AdQBsAGUAdQByAFcAYQByAG4AYQAgAEwAQwBEAEwAQwBEACAAZQBuACAAYwBvAGwAbwByAEwAQwBEACAOKg41AEYAYQByAGIALQBMAEMARABDAG8AbABvAHIAIABMAEMARABMAEMARAAgAEMAbwBsAG8AcgBpAGQAbwBLAG8AbABvAHIAIABMAEMARAOIA7MDxwPBA8kDvAO3ACADvwO4A8wDvQO3ACAATABDAEQARgDkAHIAZwAtAEwAQwBEAFIAZQBuAGsAbABpACAATABDAEQwqzDpMPwAIABMAEMARABMAEMARAAgAGEAIABDAG8AcgBlAHMAAHRleHQAAAAAQ29weXJpZ2h0IEFwcGxlIEluYy4sIDIwMTUAAFhZWiAAAAAAAADzFgABAAAAARbKWFlaIAAAAAAAAHHAAAA5igAAAWdYWVogAAAAAAAAYSMAALnmAAAT9lhZWiAAAAAAAAAj8gAADJAAAL3QY3VydgAAAAAAAAQAAAAABQAKAA8AFAAZAB4AIwAoAC0AMgA2ADsAQABFAEoATwBUAFkAXgBjAGgAbQByAHcAfACBAIYAiwCQAJUAmgCfAKMAqACtALIAtwC8AMEAxgDLANAA1QDbAOAA5QDrAPAA9gD7AQEBBwENARMBGQEfASUBKwEyATgBPgFFAUwBUgFZAWABZwFuAXUBfAGDAYsBkgGaAaEBqQGxAbkBwQHJAdEB2QHhAekB8gH6AgMCDAIUAh0CJgIvAjgCQQJLAlQCXQJnAnECegKEAo4CmAKiAqwCtgLBAssC1QLgAusC9QMAAwsDFgMhAy0DOANDA08DWgNmA3IDfgOKA5YDogOuA7oDxwPTA+AD7AP5BAYEEwQgBC0EOwRIBFUEYwRxBH4EjASaBKgEtgTEBNME4QTwBP4FDQUcBSsFOgVJBVgFZwV3BYYFlgWmBbUFxQXVBeUF9gYGBhYGJwY3BkgGWQZqBnsGjAadBq8GwAbRBuMG9QcHBxkHKwc9B08HYQd0B4YHmQesB78H0gflB/gICwgfCDIIRghaCG4IggiWCKoIvgjSCOcI+wkQCSUJOglPCWQJeQmPCaQJugnPCeUJ+woRCicKPQpUCmoKgQqYCq4KxQrcCvMLCwsiCzkLUQtpC4ALmAuwC8gL4Qv5DBIMKgxDDFwMdQyODKcMwAzZDPMNDQ0mDUANWg10DY4NqQ3DDd4N+A4TDi4OSQ5kDn8Omw62DtIO7g8JDyUPQQ9eD3oPlg+zD88P7BAJECYQQxBhEH4QmxC5ENcQ9RETETERTxFtEYwRqhHJEegSBxImEkUSZBKEEqMSwxLjEwMTIxNDE2MTgxOkE8UT5RQGFCcUSRRqFIsUrRTOFPAVEhU0FVYVeBWbFb0V4BYDFiYWSRZsFo8WshbWFvoXHRdBF2UXiReuF9IX9xgbGEAYZRiKGK8Y1Rj6GSAZRRlrGZEZtxndGgQaKhpRGncanhrFGuwbFBs7G2MbihuyG9ocAhwqHFIcexyjHMwc9R0eHUcdcB2ZHcMd7B4WHkAeah6UHr4e6R8THz4faR+UH78f6iAVIEEgbCCYIMQg8CEcIUghdSGhIc4h+yInIlUigiKvIt0jCiM4I2YjlCPCI/AkHyRNJHwkqyTaJQklOCVoJZclxyX3JicmVyaHJrcm6CcYJ0kneierJ9woDSg/KHEooijUKQYpOClrKZ0p0CoCKjUqaCqbKs8rAis2K2krnSvRLAUsOSxuLKIs1y0MLUEtdi2rLeEuFi5MLoIuty7uLyQvWi+RL8cv/jA1MGwwpDDbMRIxSjGCMbox8jIqMmMymzLUMw0zRjN/M7gz8TQrNGU0njTYNRM1TTWHNcI1/TY3NnI2rjbpNyQ3YDecN9c4FDhQOIw4yDkFOUI5fzm8Ofk6Njp0OrI67zstO2s7qjvoPCc8ZTykPOM9Ij1hPaE94D4gPmA+oD7gPyE/YT+iP+JAI0BkQKZA50EpQWpBrEHuQjBCckK1QvdDOkN9Q8BEA0RHRIpEzkUSRVVFmkXeRiJGZ0arRvBHNUd7R8BIBUhLSJFI10kdSWNJqUnwSjdKfUrESwxLU0uaS+JMKkxyTLpNAk1KTZNN3E4lTm5Ot08AT0lPk0/dUCdQcVC7UQZRUFGbUeZSMVJ8UsdTE1NfU6pT9lRCVI9U21UoVXVVwlYPVlxWqVb3V0RXklfgWC9YfVjLWRpZaVm4WgdaVlqmWvVbRVuVW+VcNVyGXNZdJ114XcleGl5sXr1fD19hX7NgBWBXYKpg/GFPYaJh9WJJYpxi8GNDY5dj62RAZJRk6WU9ZZJl52Y9ZpJm6Gc9Z5Nn6Wg/aJZo7GlDaZpp8WpIap9q92tPa6dr/2xXbK9tCG1gbbluEm5rbsRvHm94b9FwK3CGcOBxOnGVcfByS3KmcwFzXXO4dBR0cHTMdSh1hXXhdj52m3b4d1Z3s3gReG54zHkqeYl553pGeqV7BHtje8J8IXyBfOF9QX2hfgF+Yn7CfyN/hH/lgEeAqIEKgWuBzYIwgpKC9INXg7qEHYSAhOOFR4Wrhg6GcobXhzuHn4gEiGmIzokziZmJ/opkisqLMIuWi/yMY4zKjTGNmI3/jmaOzo82j56QBpBukNaRP5GokhGSepLjk02TtpQglIqU9JVflcmWNJaflwqXdZfgmEyYuJkkmZCZ/JpomtWbQpuvnByciZz3nWSd0p5Anq6fHZ+Ln/qgaaDYoUehtqImopajBqN2o+akVqTHpTilqaYapoum/adup+CoUqjEqTepqaocqo+rAqt1q+msXKzQrUStuK4trqGvFq+LsACwdbDqsWCx1rJLssKzOLOutCW0nLUTtYq2AbZ5tvC3aLfguFm40blKucK6O7q1uy67p7whvJu9Fb2Pvgq+hL7/v3q/9cBwwOzBZ8Hjwl/C28NYw9TEUcTOxUvFyMZGxsPHQce/yD3IvMk6ybnKOMq3yzbLtsw1zLXNNc21zjbOts83z7jQOdC60TzRvtI/0sHTRNPG1EnUy9VO1dHWVdbY11zX4Nhk2OjZbNnx2nba+9uA3AXcit0Q3ZbeHN6i3ynfr+A24L3hROHM4lPi2+Nj4+vkc+T85YTmDeaW5x/nqegy6LzpRunQ6lvq5etw6/vshu0R7ZzuKO6070DvzPBY8OXxcvH/8ozzGfOn9DT0wvVQ9d72bfb794r4Gfio+Tj5x/pX+uf7d/wH/Jj9Kf26/kv+3P9t//9wYXJhAAAAAAADAAAAAmZmAADypwAADVkAABPQAAAKDnZjZ3QAAAAAAAAAAQABAAAAAAAAAAEAAAABAAAAAAAAAAEAAAABAAAAAAAAAAEAAG5kaW4AAAAAAAAANgAAp0AAAFWAAABMwAAAnsAAACWAAAAMwAAAUAAAAFRAAAIzMwACMzMAAjMzAAAAAAAAAABzZjMyAAAAAAABDHIAAAX4///zHQAAB7oAAP1y///7nf///aQAAAPZAADAcW1tb2QAAAAAAAAGEAAAoCIAAAAAzSOHAAAAAAAAAAAAAAAAAAAAAAD/wAARCABMAVwDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9sAQwACAgICAgIDAgIDBQMDAwUGBQUFBQYIBgYGBgYICggICAgICAoKCgoKCgoKDAwMDAwMDg4ODg4PDw8PDw8PDw8P/9sAQwECAgIEBAQHBAQHEAsJCxAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQ/90ABAAW/9oADAMBAAIRAxEAPwD9vvEOpSaRo9zqMS72hUEDOM81+bcn7cPiSOR4/wCx87SRnzk7H/rlX6J+N/8AkV7/AP3B/MV+ANz/AMfMv++386/ojwW4Ty/MqWIljqSm4uNr3637M/DvF3ifH5fVoRwdVxUk72t5H3T/AMNyeJP+gN/5GT/41R/w3J4k/wCgN/5GT/41XxTofh7V/El4tho0BuJ2zhQQOgz3x2Fdn/wp34if9Alv+/if41+xYjw/4YpS5KtKEX2cmv1PyihxzxFVjzU6smvJJ/ofUa/tyeIw2W0bI9POX/41W7Yft4XUbBb3wv5oPU/agP5RV8a3Pwr8d2i7p9MZQP8AbQ/yNcFPbz2zmKdCjDsRiin4a8M4hWp0Yv0k/wBJDn4hcQ0H+8rSXrFfqj9w/hh8c/BHxSh8vRroLfx/623If5M7sfMyqDkLnivZ6/nr8OeIdV8L6vBrOjzmC5gOVYAHqMHg5HQmv3N+E/jqL4i+BdO8VRp5Zug4K5zgo7J1wOu30r+ffFHw1jk0o4nCtujJ213i97eafT01P3Lw38QZZspYfEpKrFX02a7+T7/gej0UUV+Pn6qMlkEUbSN0UEn8K/OXxb+2tf6d4gu7PRdM+0WcRUI/mhc8DPDR565r6q/aG8dnwH8NdR1G2fbeuEEK+v7xFbqCOhr8RZHMkjSN1Yk/nX9B+DXAGEzCjWxmPp80b8sU79NW9PkvvPwvxZ43xOBrUsJganLK15NW9Evz/A+7R+3H4jJAOj4Gf+eyf/Gq/QP4Z+N7X4ieDbDxZar5aXgf5c5xsdk7genpX4GV+jf7Enj8st/4Jv5fu+X9lX/v679B/M19H4qeGmAw+VvF5dR5ZQabtfWOz3vto/S54Hhr4hY2vmSw2Pq80ZqyvbR7r79vuP0Vooor+WT+kwrivHHxA8MfD3SH1nxNdi2gQf3WYnkDooJ6kdq627uY7O2kuZThIwSa/D743fFPU/if4vuNQmkK2ERxBFxhPlVW5CqTkrnmv0fw34DlnmLcZvlpQs5Nb67Jeb/A+A8QONY5NhlKCvUn8K6abt+SPp7xd+3FqNwz2vhjR/soX7s/nB93T+FovqK8aX9rr4zi7Ex1jMPP7vyYPT18uvmOKKWZxHEpdm6ADJr3TR/2aPjNrtot9pug+bC/IYzwr7dGcGv6gfBvDOWU1GvSpxT6zad/nJ/kfzl/rZxDmNRujUqSa6Qv+Uf1Ops/2u/jNDOJLrV/Pjz9zyYF/UR19M/Dr9tfRNTni07xxZf2avQ3W9pMk5I+SOL6Cvg/xn8IviB8P1VvFWlmzD5wRIknTH9xm9RXmvSljPDzh7NKHNRpRs9pU7L8tH87hhOOs9y2tarUldbxnd/nqvlY/of0rVdP1qxj1HTJhPbyjKsOM4OOh5rRr8qP2RvjBeaB4mTwPqshksNRz5bH/ll5ayOeikncT68V+qwIIyOhr+SuOOEKuS454WbvF6xfdf59Gf1BwbxVTzfBrEwVpLSS7P8AyPjH47/tJax8K/FA0Oy0/wC0R/3vMVc/IjdCjf3q8O/4bk8Sf9Ab/wAjJ/8AGq5n9tD/AJKGv+f+WUdfGwBJAHev6U4I8O8mxWU4fEV8OnKUU27vX8T+feMePM2w+Z16FGu1GMmktP8AI+7/APhuTxJ/0Bv/ACMn/wAao/4bk8Sf9Ab/AMjJ/wDGq+Rrb4c+MbuFbi309mjfodyj+tWP+FYeN/8AoGt/32v+Ne2+AeF07OlD/wACf/yR46434jauqs//AAH/AIB9Y/8ADcniT/oDf+Rk/wDjVJ/w3J4k/wCgN/5GT/41Xyf/AMKw8b/9A1v++1/xpP8AhWHjf/oGt/32v+NL/ULhb/n1D/wJ/wDyQ/8AXbiT/n5P/wAB/wCAfrT+z58YNR+LelX1/qFp9lNr5ePnDZ3lx2Vf7tfRNfGn7HPhfXvDnhbUH1m1Nutz5flksp3bXkzwCcda+v7+9g06zlvbltscQyTX8ocdYHD0c4r0MElyJpJLVbL9T+muC8ZXq5VRrYxvnabbenV/oZPijxXoPg3SJtc8RXQtbODG5iCx5IUcKCTyR2r4b8YftxWNpdS2XhfR/tcS423HnFM8An5Hi+or5J+Onxi1n4reKJJ5ZDHpdqSLaAYITKqH+YKpbJXPPSvE7W0uL2dba1QySOcADvX9AcF+CmDpYeOIzdc02r8t7Rj92779Pzf4dxd4vYurXlRyt8sFpzWu5eeuy7dfyPtIftseNhJuNnlfTfH/APGq9x+HX7afhvXrtdP8Y2X9jDgCfzGm3E5P3UjGMcD8a+DfEPwP+JvhbRv7f1vRzBYgA7xLG/UhfuqxPU+leT8g19VV8NOGsxoNYaEe3NCWz+Ta++581S8QuIMBWTxE335Zrdfcn91j+ii2uoLyBLm2cSRuMhh3qevz0/Yv+KV3dQTfD3VZi6QY+xg9RnzJJOQM/mfpX6F1/JXF3DVXKcfUwVV3ts+6ez/z8z+oOFuIaeaYKGLpq1912a3X9dAoorjvHnjPSfAPhi88TazII7e1C5PJ5Zgo4AJ6kdq8HDYapWqRpUleUnZJdWz28RiIUacqtR2jFXb7JHjf7Qnx3t/hLpkNrYKJ9UvN3ljONuwoT1Vhyrd6+Oh+2t45z/x7f+PR/wDxuvmH4h+ONT+IXiq88Taqxaa5K8ccBFCDoAOgHauIHUV/ZvCvhHlmGwUKeNoqdXeTfd9F5Lb8T+SeJfFDMcRjJzwlVwp7RS7d35vf8D93Pgx40vviD8O9L8V6inlz3vm7lyDjZIyDkADoPSvU6+ev2Wf+SIeH/wDt4/8AR719C1/JPFOGp0cyxNGkrRjOSS7JN2P6h4axE62XYerVd5ShFt920j//0P2t8b/8ivf/AO4P5ivwBuf+PmX/AH2/nX7/AHjf/kV7/wD3B/MV+ANz/wAfMv8Avt/Ov6r+j1/BxfrH9T+cPHb+NhvSX5o+s/2OAD8T4FYAg7uv/XKSv1y8qL+4v5V+HXwP+J1t8LPF8PiC6s/tca5yN5TGUZeyt/e9K+0/+G59DyR/YP8A5MN/8arz/FrgbNcwzX6xg6LlDlSvdb692js8L+MsswGWuhi6qjLmbtZ7adkfeRhhPVF/IV+fv7bPg7RI9N0/xLbwrFenzN5Gfn5iUcZwMD2qW6/bt0yJT9n8N+af+voj+cVfHXxk+NviL4warFcakPs9ha5+z22Vby94UP8AOFUtkrnnpXD4aeHGd4TNaeLxEPZwje+q102sm/x0OzxC4/yfFZbUw1CXPOVraPTXe7S/A8Vr9d/2N0lHwugZ/uHdt/7+yZr8qvC3hfWPGGt22haHbm5u7gnaoIXhRuPLEAcA96/c34XeBrb4deCtP8KWzb1tA+WxjJdy/Qk/3vWvsfHrOKMMvp4K/vyknbslfX79F8+x8r4J5VWnj54u3uRi1fzdtD0GiisTxHrVp4e0W61e+fZDAvJ9ycDpnua/k2lSlOShFXb0P6eq1Iwi5ydktT8zv21fHi6t4rtfB1s+P7H3+aB385I3Xt7epr4eRGkcRoMsxwB9a6Txl4jvfFniO817UG3T3LDJ46IAo6AdhXpv7PHggeOPiZpmnXMe+yUyGZv7uI3Ze4PUV/fGTYOlkOSRjPalC8vN2u/vex/EebYqrnWbylDepKy8lsvuW55P4i8Oax4U1WXRddg+zXkAUum4NjeoYcqSOhFdT8KfGc3gLx1pviaHk2zOMf8AXRCnofX0r64/bZ8BpYajpni2wiz9rEguGHby1iROp/kK+A1JUgjqK14czalnmURrTWlSLUl57Nf10M8/yupk+aSowetOScX5bp/11P6JLG7iv7OK7gbckqggirdfKv7JXj1fFnw4h0e4k82/0nPnE8Z82SQr2A6Dtmvqqv4X4hyeeAxtXB1N4Nr1XR/Nan9m5FmsMdg6WLp7SSfo+q+T0OE+JjXy+CdSOmruuNq7RkD+IZ68dK/BC4z58m7ruP8AOv6G9UsY9SsJ7GUZWVcV+D3xN8C6l8PfF994c1LLPAwIbAAbeofsT0Detf0B9HzMaSWJwra53aS7tap/dp95+HeOmBqN4fEpe7qn2T3X36/ce4/sj6t4N0vx/nxLtF4//HozbiF/dyb+ACOmOv4V+vcTwugMJUr/ALPT9K/nVV2RgyEqR3HFey+Cfj58TPAUCWOhaqY7ResZjjbPJP3mVj1Jr6bxJ8KK+b4n67ha1pWS5ZXtp2avb0tufO+H3iZRyqh9UxNK8bt80d9e66+t9uh+2etaLp+vadNpupQiaGYYIPHTkcjnrX4veOvgb4v0fxhf6NpFgbiCEqVbcq53qG7sfWvojw1+3NqlqFg1/Q/thPWXzwmOv8KxV9S+Cv2lfhX40miso75YNQmziExyHpk/fKAdBX53kGF4j4UdWbwrnCS6O6Vuul2vnY+8zzE5BxMqcfrPJOL6qzd+muj+TZ+bfg34O/FXStftNVt9EMnkt086JeCCP71ftRb7vIj3jDbRke+KWPyJFDxbWU9CMYqWvgOOeO6+eTpzr01FwutL9bb3Pt+DeCqOTRqRo1HJTtvbpfax+TP7aH/JQ1/z/wAso6+O7f8A4+Iv95f519iftof8lDX/AD/yyjr44jfy5FfrtIP5V/Xvh1/yIsL/AIEfy1x7/wAjnE/4mfvj8OIom8GacSi52t2H9413Hkw/3F/IV+aXhr9t1PD+iWuk/wDCKecYARu+17c5OenlH1rd/wCG90/6E/8A8nf/ALVX8v5h4TcQzr1Jww+jbt70O/8AiP6MwHihkUKEISr6pL7Mu3+E/RDyYf7i/kKPJh/uL+Qr87/+G90/6E//AMnf/tVOT9vVXdUHg/7xA/4/fX/tlXG/CLiL/oH/APJof/JHWvFTIf8AoI/8ln/8ifoiqqowoAHtXzH+1h4sbw78K760tpPLur7YIz/uSxluxHQ19A+HNZHiDRrbVxF5P2gE7M5xg464FfB37d2ovFbeGbBR8s32rJ/3fKNcHh1lPt8/w+HqraV3/wBu3f6HZx9mfsckr16b3jZf9vWX6n5ukliSepr9HP2K/hrYvBd+OtVgEsp2fY2J4T/WRycA4OfcfSvzjAya+q/Dn7S+seA/hjpvgrwbF9lv7fzfMusq/wB6UuPkdCOhI61/WniPlmPxuXfUsv8AiqNJu9ko6ttvtolpvc/mDgLMcFg8f9bx3wwTaVr3lsl663V7bH69XVvBdW8lvKqsrqRg+9fmLq/7GHiu88Q3Zspfs9gWBjbCNnIyeDJnrXhjftMfGdn8w6/z/wBcIf8A4ivbfhj+2X4q028Sz8f/APE0gkOPO+SLy8bv4Y4yTngV+R5RwDxPkFKrVy+cJuSV0rt6drpK5+pZrxvw7ndSnSx8JxUW7PS2vezbse2fB39k2/8Ahv40sPGNxr/nGy8zMH2cLu3oyfeEhxjOelfb1YHhnxNo3i7R4Nd0G4FzZ3AJVwCvQlTwwB6g9q36/DOKOIMfmOJ9rmLvUj7uyVrN6WSXVvc/ZuHMiwWAw/s8ArQl727d7pa3bfSw13WNDI5wqjJNfkX+1N8aT498Rnw5osm7SNOJCuOBL5iox4Kgjaynua+sP2rfjT/whPh4+FdCl26rqAILj/ljsaNujKQdyk9+K/JtVlupwoy7yN+JJr938EuBbL+2cVH/AAJ/jL9F832Pxbxh4zu/7Jwz/wAb/KP6v5eZFg4zQOor618e/ByL4cfB2x1LVbfy9a1EOZvmzt2TKF6MynKt2r5KHUV++5JnlDH05VsO7xUnG/e2ja8ux+IZvk9bBVI0q6tJpO3a+tn5n7Vfss/8kQ8P/wDbx/6PevoWvnr9ln/kiHh//t4/9HvX0LX8G8Zf8jfF/wDXyf8A6Uz+1uEf+RVhf+vcP/SUf//R/a3xv/yK9/8A7g/mK/AG5/4+Zf8Afb+dfv8AeN/+RXv/APcH8xX4A3P/AB8y/wC+386/qv6PX8HF+sf1P5w8dv42G9JfmiNI5JDtjUsfQDNTfY7v/ng//fJ/wr6r/Y9t4bj4n26zIHHzcEZ/5ZSV+tH9iaT/AM+qflX1XHPi0slxv1N4fn0TvzW3v05WfM8G+GDzfB/WlX5NWrct9rea7n89ZtLodYXH/ATULKynDAg+9f0IXXhjQbxDHcWaMp7dP5V8ofH/APZv8Kav4Xu9e8L2QttWtwpGGZt+WQH7z7RhQe1eTkPjvg8ViIUMRQdPmdr3TSv30Wh6ed+C2Kw1CVahWU+VXtaz07as/Pj4Q/F3W/hLrh1LTVE1tcEefCQo3hQwX5irEY3Z461+x/w0+JOhfE3w/HrmjOMkfOnzfJ8zKOWC5ztNfgs6lHZD1UkflX2L+xn4wu9H+If/AAjMZPka198f9cI5GHY9z6iu/wAXuBMPjcFUzKkrVqau3/Mlvf0Wz+RxeFvGlfCYungKjvSm7W7N7W9Xv95+tNfF/wC2d48XQvA8XhOJv3uubunUeQ8b+h9fUV9nMwRSx6AZr8Wf2mfHbeNfiXfG3l8ywtSggGOm6NA/YHqO9fh/g7w99eziFSa9yl7z9fs/jr8j9i8V89+p5VKnF+9U91enX8NPmfPHU1+n37EvgZ9N8PX3jK6XK6p5fkH08ppUbv8AzFfmBXsHhn48fFLwholv4d8P6x9lsLXd5cfkxNjcxY8spJ5J6mv6i8QuH8ZmmXSwWDmouTV+a+y16J9bfI/nHgXPMLl2PjjMXFyUU7Wtu9OrXS/zP1t+O/gn/hPPhxqeiQjNw4Qxn02yKx7jstfhtLGYpXjPVSR+Ve8n9p341kFT4g4YEH/R4Oh/4BXg800k8rTSnLuck+9eP4YcH4/JaFXDYucZRbTjy30ezvdLyPW8RuKsFm9eniMLCUZJWd7arps35/gfUv7JXj6Twn8R4dHmfy7HV8+cfTyo5CvYnqe1fsKCCAR0NfzuWF3LYXkV3A2142BBFfvB8KvGdv4/8C6b4nt/u3SuMf8AXN2T0H930r8o8fOHPZ16WZ01pL3Zeq2+9afI/TPBHP8Ano1cum9Y+9H0e/3P8z0SvGvi78FPC/xb0xIdVQQ39vn7Pc/MfL3Fd3yhlDZC456V2Pjb4g+FPh5pw1XxZe/Yrc9G2O/cDogJ6kV4Rf8A7YHwit1JstQ+0nsPLmX+cdfkXDeUZy6kcZllKd09JRTt9+3qvvP1LiDNMp5JYTMakLNaxk1f7t/Rnwv47/ZU+JnhO4ml0+z/ALS06LGLgPFHkHH8BcnqcV84XWmX9lK0FzA6OnUYP86/dv4a/EjRPifoK63pAHlnO5eTj5mUfeVf7vpXS614T8P+Ibc2urWaTRnqOV/VcHtX7JgfHLH4Oo8LmuGTlHR291/Napv0sj8nxng1gsXBYjLMRaMtVfVfJ6NL72fz4kEdacjuh3ISpHpX6/8Ai/8AZB+F2uW0n9hWv9k3T/8ALXdLN6fwtIB2/WvzP+K/wt1n4VeI5NE1M+bEceVN8o8z5VY/KGbGN2Oa/YeEvEnLM5k6WHbjPflkrP5bp/ffyPynifw/zDKYqrXScNuaLuvns/wPoD9mj9ojUfCOqW/g7xROZdFmJEbnH7jAdzwqFm3MR34r9XUZXUOpyGGRX860DFJo3U4IYH9a/eD4Qazea/8ADzSdUv23zyrIGPH8Lso6AdhX4p47cKYfDVKeY4ePLztqSXV7p+r1v/nc/XvBfiavXhUwFd35EnF9ls18tLf8MfnJ+2h/yUNf8/8ALKOvjWvsr9tD/koa/wCf+WUdfHdv/wAfEX+8v86/c/Dl/wDCHhf8KPxrj1f8LOJ/xMcLS7YZWFyP900v2K8/54Sf98mv3m+G9panwZpxMKE7W/hH94+1dz9jtP8Angn/AHyP8K/JsX9IH2VWVP6neza+Pt/24fp+F8DfaUo1PrdrpP4O/wD28fzwfYrz/nhJ/wB8mprezvPtEX7iT7y/wn1r+hn7Haf88E/75H+FH2S0/wCeCf8AfI/wrmf0iNP9y/8AJ/8A7Q6F4D/9Rf8A5J/9scf8OAV8GaaGGDtb/wBCNfDP7ef+u8Kf9vf8oq/R0AAYAwK/O39vGzkZPC10OVT7Xn2z5Qr898KcUqnE1Gq9OZzf3xkfc+JmHdPh2rTWtlBfdKJ+cVeqfCn4TeIvivrf9maMm2CLHnzfKfL3KxX5WZc52kcV5XX6ufsVWNjB4FubyGHZcXG3zHyTu2vKBx2wPSv6h8RuJ6uU5XPFUFed0lfZN9fl+Z/OXAXDtPM8yhhqztHVvzt0PGPF/wCw9rejaDJf+HNa/te+iAIt/IWHfyM/M0uBgZP4V8I3NtNZzvbXClJIzgg9q/ooIBBB6GvxX/ai0aDRvjFrUNsAsTeThR2/coT/ADr858IPEXHZniKmCx8uZpcydknuk1pZddD73xU4CweXUKeLwS5U3ytXb6Np63fTX+r+n/se/FG40Dxb/wAIXfOWtNV+6T/yz8pJHPYk5J9a/Rz4kePdK+HXhS78S6ow2QAYXnJLMF7A9Cw7V+J/wsv5dM8daZeQffRnx+KEV9P/ALafiLVLvxNp+iSyn7Jaq2xOMfPHEx7ZPPrWHG/AFHH8TYePwxqRcp+fLv8ANqy/E34O44rYHh6u95QklHy5v0Wr/A+SPGni3VvG/iO78R6zKZrq6I3NgDhVCjgADoB2r6w/ZK+Cz+KNbHjbXY8adY8xIf8AltvWRDyGBXawHbmvidCFcMRkAg4r7P8ACn7X954O0G28PaP4fEVrahgo88H7xLHkxk9Se9fpHHOCzF5b9RyamryXLulyxt0v328j8/4NxeAWYfXM2m7J82zfNLz/AD8z6E/bXUL4JsVUYA3/APocVflWOor6U+L/AO0bqfxa0iDSbvTvsiw7vm8wPncVPQIv92vmscHNR4Y5BistyqOFxcbTTb3vu/IvxEzzD5hmcsThXeLSW1tkftV+yz/yRDw//wBvH/o96+ha/Iv4b/tX6z8P/CNh4QtdL89LPeBJ5qrne5foYz6+tfp58OPFM/jPwdp/iS5h8iS7D5TO7G1yvUAenpX8v+I/BuPwWMrY3ERShUqS5XdPdtrT0P6M8P8AizBYvCUsHQk3OnCN9H0ST/E//9L9rfG//Ir3/wDuD+Yr8Abn/j5l/wB9v51+/wB43/5Fe/8A9wfzFfgDc/8AHzL/AL7fzr+q/o9fwcX6x/U/nDx2/jYb0l+aPrD9jqSOP4oW5kYKPm5Jx/yykr9cftlp/wA90/76H+NfztxTSwNvhYow7g4qx/aF9/z3f/vo19Rx34S/21jvrn1jk0Sty32v15kfNcGeKH9kYP6r7Dn1bvzW3t0sz+hs3tmoyZ0wP9of4182ftA/G7wv4I8J3emw3In1e5AEMADfNhkLfNtKjCnPNfjsb+9IwZ3/AO+jUDyyynMjlz7kmvByXwDoYfEwrYjEOcYu9lHlvbu7vQ9vN/G2vXw86NDDqEpK1+a9r+VlqJI/mSM/94k/nX1H+yHpV7d/F/TNRgTdDZ+b5jccb4ZAK+XoYZZ5FihQuzHAAGa/Xn9lb4OXPw78Lya1rkflarqoUyRZB8vy2cL8ysQdysDX3HitxFRwGT1YSfv1E4xXe+jfyX6dz43w0yGrjs1pTivdptSb7W1X3v8AU9Z+NfjgfD/4ean4hQ/volQIvrvkVD2P970r8LZ5WnmeVjkuSfzr79/bb8ei71LT/BljLj7H5n2pf728ROnUfyNfn8OTj1rx/BPh36nlP1ma96q7/wDbq0j+r+Z63i/n31vM/q8H7tJW+b1f6L5HpHw7+FXjD4n3Fzb+FLT7SbTb5nzou3eCR99l/umvXP8AhkL4x/8AQL/8jQ//AByvs79jvwOnh34eDxHIm241r749PJkkUdz2PoK+va+E4x8acfhMyrYbBRi4QdrtO91v173Ps+FPCLB4rL6WJxcpKc1eyatZ7dO1j8dP+GQvjH/0C/8AyND/APHK5Lxr+zp8TPAegz+JNe07yrG22+Y/mRNjcwUcK5PUjtX7cVy3jTw1ZeLvDN7oF+u6G5UZHPVCGHQjuK8TL/HjNPbw+sQhyXV7J3tfW2u9j18d4KZcqM3QnPns7XatfpfTY/n4r9Gf2JPH+ft/gnUJv+ef2RMf9dZH6D+Zr8/tf0i80LV7nS7+PypoW5XIOAeR0z2rrPhV4ym8BeOtN8TwjLWrOMZ/56IU9D/e9K/ofjfIo5tlFXDw1bV4+q1X37ejPwrg/OZZZmlKvLRJ2l6PR/dv6o/Uv9rbw3/bXwnv7+JN89j5ewD/AKaSxg9/QV+OJGCR6V/Qu8Vh4j0by5QJre6QZ64P/wCo1+LXxz+D2sfCvxTJbPGZNMujm2nGMPhVL/KGYrgtjnrX5H4EcT040qmU1naablG/Xul6Wv8AN9j9Q8aeHajqwzSkrwaUZW6Po363t8vM9H/ZZ+Nun/DbW59E8SP5elamVBm5Pk+WrkfKqsW3MwHtX60WWp2Go26XVnOskUgyCD/Sv53eR0rsPD/j3xX4X3f2JftBu65Afpn+8D619Jx/4QUs2xDxuGqclR7pq6fn5O3r/n8/wR4qVMroLCYinz01tZ2a8vNXP3v1DVtO0u1kvL6dYooxliTn9BzX48/tOfFfSfib4wUaB+90/TiRFPyPM3om75WVSMFSOa8T8QeOfFPil1fW75pyvTACdcf3QPQVyVbeHvhLTyev9cr1eepaysrJX39X93oZ8deJ882o/VKNPkp3u7u7dtvT8fUkiBaVAOpYfzr91vglY3em/DPR7O9j8uZFlyuQcZkYjp7V+Wf7PXwW1H4neKI57qAjRrM5uJCQMblbZxuVj8y9q/Z2GJIIkhjGFQAD8K+E8fOIqNR0cupu8ovml5aWS9d39x9n4I5DWgquPqK0ZLlj566v8l95+T37aH/JQ1/z/wAso6+O7f8A4+Iv95f519iftof8lDX/AD/yyjr41r9o8OV/wh4X/Cj8k49f/Czif8TP3v8AhxdWo8GaaDMgO1v4h/ePvXc/bLT/AJ7p/wB9D/Gv54lv71VCLM4A6fMaX+0L7/nu/wD30a/JcX9H72tWVX65a7b+Dv8A9vH6dhfHL2dONP6peyS+Pt/26f0OfbLT/nun/fQ/xo+2Wn/PdP8Avof41/PH/aF9/wA93/76NH9oX3/Pd/8Avo1h/wAS7/8AUZ/5J/8AbnR/xHj/AKhP/J//ALU/ogjmilz5Tq+OuCDXyt+174WbW/hddarCN0um7do9fNljB7+1eG/sKXl1NeeJ45ZWdf8AReCc9pa/Q3V9LtNa06fTL5PMhnXBB9uR0x3r8px+Clw1n8YKfP7Jxd7Wumk2rXfR2P0vBYxcQ5JKbhye0Ula97NNpO9l1Vz+eIjBxX6LfsW/E3T4EufAeqzCGZ9n2QEffx5kknIXAx7n6V8j/GH4Ta78LPEkun6gpktJSTBPgAPwrN8oZsYLY5615NBPNbSrNA5R1OQRxX9d59lOE4iyp0oT92aTjJa2fR/o16rQ/lrJczxWRZmqk4e9BtSi9Lrqv1T+ep/RHPcQ28LTyuFRBkknivw8+Pvi+z8b/FDV9e02TzLWYxBDgj7kSoeoB6j0qjrvxv8AiZ4k0M+HNY1gzWBAHliKNDwQ33lUHqPWvKQGdsDkmvjfDPwvqZJWqYrE1FKbXKrbJXv16uy9PM+s8Q/EaGcUqeHw9Nxgnd33btbp0V/n5Hpnwd0S48Q/ETStKtjiSZpCDjP3UZvb0r6c/a18IeItZ8cxy6bZmaNVA3Agf8sox3PtXafsdfBy6tpD8R9dtzH/AM+JJ5P+sjk4DcdvvD6V+h7RRucugY+4zXxHHfiZHBcQRq4aKmqUXF66cz31XbRetz7Hgvw8li8jlTxEnB1ZKS06LbTzu36WPwN/4Vp41/6Brf8AfS/40f8ACtPGv/QNb/vpf8a/fDyIf+ea/kKPIh/55r+Qrl/4mFxP/QLH/wACf+R0/wDECqH/AEEv/wABX+Z/PprXhfXfD6xPq9qYBNnaSQc7cZ6Z9a5+v1U/bWRE8FWexQM7+gH9+KvysHUV+6cCcUTzjL442cOVttWTvsfjPGnDccqx0sJGfMkk72tujrtL8D+J9UtotQsbJpbeQ/KwKjODg9T61+1XwRsrrTvhno9peR+XMgl3KecZkY9q479lyOOT4JeH2dFYn7RyQP8Anu9fQ4AUYAwK/mfxU4/qZjVll8qSiqU5a33tdH9CeGnA8MDShj41G3VgtLbXsz//0/3D1vTBrGmT6aX8vzxjdjOOc9OK+AJf2CRJI8n/AAmWNxJx9i9f+2tfopRX2HD3GeZZVGccBV5VK19E9vVM+cz3hLL8zcZY2nzOO2rW/o0fnR/wwMP+hy/8kv8A7bR/wwMP+hy/8kv/ALbX6L0V9J/xGHiL/oJ/8lh/8ieB/wAQqyH/AKB//Jpf/JH50D9gYA8+Ms/9uX/22tWy/YR02GRWu/E32hR1X7KVz+UtfoFRUT8XeIZKzxP/AJLH/wCRHHwsyJO6w/8A5NL/AOSPBfh7+zr8Ofh9cRalp9iJdRjzictJ3BH3S7DocV7zj5do4paK+HzPN8VjKntsVUc5d27n2WXZXh8JT9lhqajHslY+H/iL+x1P8QfF194rufFn2d73ZmP7Ju27ECdfMHp6VxcX7BIjkSQ+MchSDj7F1wf+utfopRX2eF8Vs+o0o0aWItGKSS5YaJaL7J8liPDPJatSVWpQvKTu3zS3fzMvRdItNC0yDSrFdsMAwB9Tk9c961KKK/PqlSU5OUnds+5pwUYqMVZIKKKKgs+Mfit+yFZ/Ejxhd+LLbX/7LN5s3RfZjL9xFTr5i+melebr+wRtYMPGXI5/48v/ALbX6LUV+hYLxUz3D0YUKWItGKSXuxei23Vz4XF+GuS16sq9WheUnd+9Jav0ZxXw98KXPgjwlY+Gru//ALSktN4M5Ty925y33QTjGcda1/EfhnRPFmly6Nr9sLq0nxuUkqeCCOVII5A71vUV8RUx9aVd4lytNu91pre91bb5H19PA0o0Vh1G8ErWeum1nff5nwX4x/Ye0LVLx7zwvq/9lQ9oPJaXsP4nl9cmvIX/AGJPGS3qwJfboDnMuxBjjjjzc8niv1Sor9EwHjDn1CHs/bcy/vJN/fa7+dz4TG+FOS1p8/suV+TaX3bL5WPypsf2J/G082y8vPsyf3tkbfoJa9s8GfsReGtIuRc+KtT/ALYj/wCeXlNDjr/Ekv0P4V91UUZj4wZ9iIOHtuVP+VJfjuvkwwHhVktCSn7Lma/mbf4bP5oxdA8PaR4X0yLR9DtxbWsOdqgk9SSeSSTya2qKK/NKtWU5Oc3dvds/Q6VKMIqEFZLZHyZ8aP2YB8XfEI17/hIP7Mx/yz+z+b/Cq9d6/wB2vG/+GBh/0OX/AJJf/ba/Reivust8T88wlCGGw9e0IqyXLF6fNXPi8f4c5Piq0sRXoXlJ3b5pfoz86P8AhgYf9Dl/5Jf/AG2j/hgYf9Dl/wCSX/22v0Xoru/4jDxF/wBBP/ksP/kTk/4hVkP/AED/APk0v/kj86P+GBh/0OX/AJJf/baP+GBh/wBDl/5Jf/ba/Reij/iMPEX/AEE/+Sw/+RD/AIhVkP8A0D/+TS/+SPmv4D/s9/8AClZ9Um/tv+1f7R8rjyPJ2eUH/wBts53V9KUUV8PnOdYnMMRLFYuXNN2u7JbK3RJbH2GU5Th8DQjhsLHlgtldvfXrdnJeMfBHhrx7o8uh+KLMXdpLjK7mQ8EMOVIPUDvXwN4x/YfvY7iS78K6t50Tn5YPKC7On8Ty89zX6TUV7XDXHOZ5TdYOraL3i9V9z2+VjyOIeDMuzOzxdO8l1Wj+9b/M/KCD9ivx7JKqTT+Wh6ttjOPw8yvo/wCGP7HXhbwndDUvFN1/bU4wUXY0Ow8g8rIQc5H5V9oUV72beLmeYyk6MqvKnvyqz+/f7jxMr8LcnwtRVVT5mtuZ3X3bfeiOKKOGNYolCoowAOgqSiivzRs/REgooopAeJ/Gz4Pj4waJDo51P+zDFu+fyvNzllbpuX+7XyoP2BgDn/hMv/JL/wC21+i9Ffa5J4h5vl1BYbB1uWC1tyxe/qmz5HOOBMqx9d4jF0eab63ktvRo89+FvgQfDbwRp/g4Xf277D5n77Z5e7e7P93LYxux1r0Kiivk8Zi6mIrTr1neUm233b1Z9NhMJToUoUKStGKSS8loj//Z"""
    val responseFuture = pipeline {
      Post("http://127.0.0.1:8080/"+myID.toString+"/feed/", PhotoMessageWithToken(message,photoData,0L,token))
    }
    responseFuture onComplete {
      case Success(ID(id)) =>
        //self ! id
        if(logActive){
          log.info("post new photo succeed, post id:"+id.toString)
        }

      case Failure(error) =>
        if(logActive){
          log.error("post new photo failed")
        }

      case _ =>
        if(logActive){
          log.error("post new photo failed")
        }
    }

  }

  def makeFriend() = {
    val pipeline = sendReceive ~> unmarshal[OK]
    val random_friend = 1000001L + Random.nextInt(userNumber)
    val responseFuture = pipeline {
      Post("http://127.0.0.1:8080/" + myID.toString + "/friends/", FriendIdWithToken(random_friend, token))
    }
    responseFuture onComplete {
      case Success(OK(msg)) =>
        friendList += random_friend
        self ! "addFriend"
        if(logActive){
          log.info("making friend succeed, return msg:" + msg)
        }

      case Failure(error) =>
        if(logActive){
          log.error("making friend failed, maybe they are friends alreay, see log of server")
        }

      case _ =>
        if(logActive){
          log.error("making friend failed")
        }
    }
  }

  def checkTimeline() = {
    val pipeline = sendReceive ~> unmarshal[TimelineNode]
    val responseFuture = pipeline {
      Get("http://127.0.0.1:8080/me/feed/?access_token="+token)
    }
    responseFuture onComplete {
      case Success(TimelineNode(_,_,_)) =>
        if(logActive){
          log.info("check timeline succeed")
        }

      case Failure(error) =>
        if(logActive){
          log.error("check timeline failed")
        }

      case _ =>
        if(logActive){
          log.error("check timeline failed")
        }
    }
  }

  def checkPage() = {
    val pipeline = sendReceive ~> unmarshal[TimelineNode]
    val responseFuture = pipeline {
      Get("http://127.0.0.1:8080/"+(1000001L + Random.nextInt(userNumber)).toString+"/feed/?access_token="+token)
    }
    responseFuture onComplete {
      case Success(TimelineNode(_,_,_)) =>
        if(logActive){
          log.info("check page succeed")
        }

      case Failure(error) =>
        if(logActive){
          log.error("check page failed")
        }

      case _ =>
        if(logActive){
          log.error("check page failed")
        }
    }
  }

  def checkOtherProfile() = {
    val pipeline = sendReceive ~> unmarshal[PersonInfo]
    val responseFuture = pipeline {
      Get("http://127.0.0.1:8080/"+(1000001L + Random.nextInt(userNumber)).toString+"/?access_token="+token+"&fields=birthday")
    }
    responseFuture onComplete {
      case Success(PersonInfo(name,_,_,_,_)) =>
        if(logActive){
          log.info("check other's profile succeed,name:"+name)
        }

      case Failure(error) =>
        if(logActive){
          log.error("check other's profile failed")
        }

      case _ =>
        if(logActive){
          log.error("check other's profile failed")
        }
    }
  }

  def checkMyProfile() = {
    val pipeline = sendReceive ~> unmarshal[PersonInfo]
    val responseFuture = pipeline {
      Get("http://127.0.0.1:8080/me/?access_token="+token+"&fields=birthday")
    }
    responseFuture onComplete {
      case Success(PersonInfo(name,_,_,_,_)) =>
        if(logActive){
          log.info("check own profile succeed,name:"+name)
        }

      case Failure(error) =>
        if(logActive){
          log.error("check own profile failed")
        }

      case _ =>
        if(logActive){
          log.error("check own profile failed")
        }
    }
  }

  def deletePost() = {
    if(!postList.isEmpty) {
      val postID = postList(0)
      postList -= postID
      val pipeline = sendReceive ~> unmarshal[OK]
      val responseFuture = pipeline {
          Delete("http://127.0.0.1:8080/" + postID.toString + "/?access_token=" + token)
      }
      responseFuture onComplete {
        case Success(OK(_)) =>
          self ! "deletedPost"
          if(logActive){
            log.info("post deletion succeed")
          }

        case Failure(error) =>
          self ! "deletedPost"
          if(logActive){
            log.error("post deletion failed")
          }

        case _ =>
          self ! "deletedPost"
          if(logActive){
            log.error("post deletion failed")
          }
      }
    }else{
      self ! "deletedPost"
      if(logActive){
        log.error("post list is empty")
      }
    }
  }

  def deleteFriend() = {
    if(!friendList.isEmpty) {
      val fID = friendList(0)
      friendList -= fID
      val pipeline = sendReceive ~> unmarshal[OK]
      val responseFuture = pipeline {
        Delete("http://127.0.0.1:8080/" + fID.toString + "/?access_token=" + token)
      }
      responseFuture onComplete {
        case Success(OK(_)) =>
          self ! "deletedFriend"
          if(logActive){
            log.info("friend deletion succeed")
          }

        case Failure(error) =>
          self ! "deletedFriend"
          if(logActive){
            log.error("friend deletion failed, maybe your have been deleted by your friend already")
          }

        case _ =>
          self ! "deletedFriend"
          if(logActive){
            log.error("friend deletion failed")
          }
      }
    }else{
      self ! "deletedFriend"
      if(logActive){
        println("friend list is empty")
      }
    }
  }


  def updateProfile() = {
    val pipeline = sendReceive ~> unmarshal[OK]
    val responseFuture = pipeline {
      Post("http://127.0.0.1:8080/me/", TokenAndPersonInfo(token, PersonInfo(Random.alphanumeric.take(6).mkString, Calendar.getInstance.getTime.toString, "female", "new place", "new place")))
    }
    responseFuture onComplete {
      case Success(OK(msg)) => 
        if(logActive){
          log.info("update profile succeed")
        }
      case _ => 
        if(logActive){
          log.error("update profile failed")
        }
    }
  }



  def receive = {
    case "posted" => {
      // do all delete operation here
      postedNum += 1
      
      if( postedNum >= (postNum_FirstBunch+postNum_SecondBunch) && deletionFinished == false){ // when all post are sent
        deletionFinished = true
        // delete post 10 times
        for (i <- 0 until deletePostNum) {
          deletePost()
        }
        // delete friend 10 times
        for (i <- 0 until deleteFriendNum) {
          deleteFriend()
        }
      }
    }
    case "deletedFriend" => {
      deletedFriendNum += 1
      if(deletedFriendNum == deleteFriendNum && deletedPostNum == deletePostNum){
        if(times > 0){
          println("active new actor")
          context.system.actorOf(Props(new SimulatedPerson(id+1, times-1)))
          self ! PoisonPill
        }
      }
    }
    case "deletedPost" => {
      deletedPostNum += 1
      if(deletedFriendNum == deleteFriendNum && deletedPostNum == deletePostNum){
        if(times > 0){
          println("active new actor")
          context.system.actorOf(Props(new SimulatedPerson(id+maxConcurrency, times-1)))
          self ! PoisonPill
        }
      }
    }
    case "addFriend" => friendNum += 1
  }
}


object Main extends App {

  // 用户总数，一般是10万
  val userNum = 100000
  // 活跃用户，指一共多少用户会发post之类的
  val activeUserNum = 3000
  // 最大并发，不能所有活跃用户一起跑
  val maxConcurrency = 100



  implicit val system = ActorSystem("facebook_api_client")
  implicit val timeout = Timeout(240 seconds)
  import system.dispatcher

  val registerActor = system.actorOf(Props[Register])


  for(i <- 1000003L until (1000003L+userNum)){
    registerActor ! "reg"
  }

  var regDone = false
  while(regDone == false) {
    val future = registerActor ? "reg_id"
    val ret = Await.result(future, Duration.Inf)
    ret match {
      case x: Int => if(x >= activeUserNum){regDone = true}else{println(x)}
      case _ => {}
    }
    var i = 0L
    while(i != 95000L){
      i += 1
    }
  }

  val bunchNum = activeUserNum/maxConcurrency+1
  for(i <- 0 until maxConcurrency){
    system.actorOf(Props(new SimulatedPerson(i+1,bunchNum)))
  }

}