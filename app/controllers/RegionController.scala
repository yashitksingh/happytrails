package controllers

import play.api.Play.current
import play.api.libs.concurrent._
import play.api.mvc.{Action, Controller}

import org.beaucatcher.bobject.{JValue, JsonFlavor, JArray, BObject}

import plugins.MongoDB

import models.Region
import org.beaucatcher.bson.ObjectId


object RegionController extends Controller {

  def getRegionFeed(urlFriendlyRegionName: String) = Action { implicit request =>
    Ok("")
  }

  def subscribe(urlFriendlyRegionName: String) = Action { implicit request =>
    Ok("")
  }

  def unsubscribe(urlFriendlyRegionName: String) = Action { implicit request =>
    Ok("")
  }

  def saveRegion = Action(parse.tolerantText) { implicit request =>

    implicit val context = MongoDB.context

    println(request.body)
    
    Ok("")
    /*
    request.body.map { jsonString =>
    
      println(jsonString)
      
      Async {
        
        val region = Region.parseJson(jsonString)
        
        println(region)

        // todo: validate

        // save
        Region.async[BObject].insert(region).asPromise.map { writeResult =>

          println(writeResult)
          
          /*
          Region.async[Region].findOneById(region._id).asPromise.map { something =>
            println(something)
            
            Ok("").as(JSON)
          }
          */
          Ok(region.toJson(JsonFlavor.CLEAN)).as(JSON)
        }
      }
    }.getOrElse {
      BadRequest(""" {"error": "something went wrong"} """).as(JSON)
    }
    */
    
    /*
    request.body.asJson.map { json =>
      // do some validation

      val region = Region.createJson(json.toString())

      Ok(region).as(JSON)
    }.getOrElse {

      BadRequest(""" {"error": "something went wrong"} """).as(JSON)
    }
    */
    
  }

  def deleteRegion(urlFriendlyRegionName: String) = Action { implicit request =>
    Ok("")
  }

  def saveRoute(urlFriendlyRegionName: String) = Action { implicit request =>
    Ok("")
  }

  def getRegion(urlFriendlyRegionName: String) = Action { implicit request =>
    Ok("")
  }
  
}


/*
@With(CurrentUser.class)
public class RegionController extends Controller {

    public static Result getRegionFeed(String urlFriendlyRegionName) throws FeedException, IOException {
        Region region = Region.findByUrlFriendlyName(urlFriendlyRegionName);

        if (region == null) {
            return notFound("Could not find that region");
        }
        
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0");

        feed.setTitle("Uber Tracks - " + region.getName());
        feed.setLink("http://hike.ubertracks.com"); // todo: externalize URL
        feed.setDescription("Updates for Hike Uber Tracks - " + region.getName());

        List entries = new ArrayList();
        
        for (Route route : region.routes) {
        
            SyndEntry entry = new SyndEntryImpl();
            entry.setTitle("Route - " + route.getName());
            entry.setLink("http://hike.ubertracks.com" + routes.RouteController.getRouteHtml(region.getUrlFriendlyName(), route.getUrlFriendlyName()).url()); // todo: externalize URL
            entry.setPublishedDate(route.creationDate);
            
            SyndContent description = new SyndContentImpl();
            description.setType("text/plain");

            // todo: templatize
            String text = "Route - " + route.getName()  + "\n\n" +
                    route.description + "\n\n" +
                    "Location: " + route.location + "\n\n" +
                    "Created: " + route.creationDate + "\n\n" +
                    "Distance (Miles): " + route.distanceInMiles + "\n\n" +
                    "Average Rating: " + route.getAverageRating() + "\n\n";
            
            for (Direction direction : route.directions) {
                text += direction.stepNumber + " - " + direction.instruction + "\n";
            }
            
            description.setValue(text);
            
            entry.setDescription(description);
            
            entries.add(entry);
        }
        feed.setEntries(entries);

        StringWriter writer = new StringWriter();
        SyndFeedOutput output = new SyndFeedOutput();
        output.output(feed, writer);
        writer.close();
        
        return ok(writer.toString()).as("application/rss+xml");
    }

    @Security.Authenticated(Secured.class)
    public static Result subscribe(String urlFriendlyRegionName) {
        User user = CurrentUser.get();
        Region region = Region.findByUrlFriendlyName(urlFriendlyRegionName);
        
        if ((user != null) && (region != null)) {
            if (RegionSubscription.findByUserAndRegion(user, region) == null) {
                RegionSubscription regionSubscription = new RegionSubscription(user, region);
                regionSubscription.save();
            }
            return redirect(routes.RegionController.getRegionHtml(urlFriendlyRegionName, "name"));
        }
        else {
            return badRequest("Could not find region or user");
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result unsubscribe(String urlFriendlyRegionName) {
        User user = CurrentUser.get();
        Region region = Region.findByUrlFriendlyName(urlFriendlyRegionName);

        if ((user != null) && (region != null)) {
            RegionSubscription regionSubscription = RegionSubscription.findByUserAndRegion(user, region);
            if (regionSubscription != null) {
                regionSubscription.delete();
            }
            return redirect(routes.RegionController.getRegionHtml(urlFriendlyRegionName, "name"));
        }
        else {
            return badRequest("Could not find region or user");
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result addRegion() {
        return ok(views.html.regionForm.render(form(Region.class)));
    }

    @Security.Authenticated(Secured.class)
    public static Result saveRegion() {
        User user = CurrentUser.get();
        
        if (!user.isAdmin) {
            return unauthorized("You don't have access to do this.");
        }
        
        Form<Region> regionForm = form(Region.class).bindFromRequest();

        // check if the name is a duplicate
        if ((regionForm.hasErrors() == false) && (Region.findByUrlFriendlyName(regionForm.get().getUrlFriendlyName()) != null)) {
            regionForm.reject("name", "Duplicate Region Name");
        }

        Http.MultipartFormData.FilePart photoFilePart = request().body().asMultipartFormData().getFile("photo");
        
        if (photoFilePart == null) {
            regionForm.reject("Photo required");
        }

        if (regionForm.hasErrors()) {
            return badRequest(views.html.regionForm.render(regionForm));
        }
        else {
            
            try {
                Region region = regionForm.get();
                S3Photo photo = new S3Photo(photoFilePart.getFile(), 300);
                region.photo = photo;
                region.save();
                return redirect(routes.RegionController.getRegionHtml(region.getUrlFriendlyName(), "name"));
            } catch (IOException e) {
                Logger.error(e.getMessage());
                return internalServerError("Error uploading photo");
            }
            
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result deleteRegion(String urlFriendlyRegionName) {
        User user = CurrentUser.get();
        Region region = Region.findByUrlFriendlyName(urlFriendlyRegionName);

        if ((region == null) || (user == null)) {
            return badRequest("User or Region not found");
        }

        if (!user.isAdmin) {
            return unauthorized("You aren't allowed to do this.");
        }

        region.delete();

        return redirect(routes.ApplicationController.index());
    }

    @Security.Authenticated(Secured.class)
    public static Result addRoute(String urlFriendlyRegionName) {
        Region region = Region.findByUrlFriendlyName(urlFriendlyRegionName);
        return ok(views.html.routeForm.render(region, form(Route.class)));
    }

    @Security.Authenticated(Secured.class)
    public static Result saveRoute(String urlFriendlyRegionName) {
        
        Map<String, String[]> urlFormEncoded = request().body().asFormUrlEncoded();
        Map<String, String> newData = new HashMap<String, String>();
        
        for (String key : urlFormEncoded.keySet()) {
            if (!key.startsWith("directions[")) {
                newData.put(key, urlFormEncoded.get(key)[0]);
            }
        }
        
        // cleanup the unfilled in values
        for (int i = 0; i <= 24; i++) {
            String instructionsKey = "directions[" + i + "].instruction";
            String stepNumberKey = "directions[" + i + "].stepNumber";
            
            if (urlFormEncoded.containsKey(instructionsKey)) {
                if ((urlFormEncoded.get(instructionsKey) != null) && (urlFormEncoded.get(instructionsKey)[0].length() > 0)) {
                    newData.put(instructionsKey, urlFormEncoded.get(instructionsKey)[0]);
                    newData.put(stepNumberKey, urlFormEncoded.get(stepNumberKey)[0]);
                }
            }
        }
        
        Form<Route> routeForm = form(Route.class).bind(newData);
                
        Region region = Region.findByUrlFriendlyName(urlFriendlyRegionName);
        
        // check if the name is a duplicate
        if ((routeForm.hasErrors() == false) && (Route.findByUrlFriendlyName(region, routeForm.get().getUrlFriendlyName()) != null)) {
            routeForm.reject("name", "Duplicate Route Name");
        }
        
        if (routeForm.hasErrors()) {
            return badRequest(views.html.routeForm.render(region, routeForm));
        }
        else {
            Route route = routeForm.get();
            route.region = region;
            route.save();
            return redirect(routes.RouteController.getRouteHtml(region.getUrlFriendlyName(), route.getUrlFriendlyName()));
        }
    }

    public static Result getRegionHtml(String urlFriendlyRegionName, String sort) {
        Region region = Region.findByUrlFriendlyName(urlFriendlyRegionName);
        if (region == null) {
            return notFound("Could not find that region");
        }
        else {
            return ok(views.html.region.render(region, sort));
        }
    }
    
}

*/