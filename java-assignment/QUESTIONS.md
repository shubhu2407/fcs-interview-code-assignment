# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
Honestly, yeah, it's inconsistent. Store and Product talk to the database directly - simple, but all the logic ends up stuffed inside the REST methods. Warehouse is set up differently, with its own model, its own
repository, and the business rules living separately.

I actually got to feel the difference firsthand. When I wrote tests for the Warehouse rules - things like "you can't go over a location's max capacity" - I could test that with a plain fake object, no database, no
app startup, just instant. If I tried to test something similar in Store, I'd have to boot up the whole thing first.

So I'd move toward the Warehouse style, but only where it's worth it. Store and Product are simple CRUD, not much logic there, so I wouldn't rebuild them from scratch. I'd just pull any real logic out of the REST methods
into small standalone pieces, so it's testable without the full setup.

One small thing I'd still clean up on the Warehouse side - the REST layer ended up reaching into the database code directly for one thing, instead of only using the clean interface. Small inconsistency, but worth fixing.
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
Warehouse's API comes from a YAML file - the code gets generated from it. Store and Product are just written directly, no separate file.

Generating it has one big plus: the docs and the code can never disagree, they're the same source. Good if other teams need to use your API too.
Downside - it's a bit more setup and can be finicky. We actually hit a random warning during the build from the generator config, took a minute to confirm it wasn't actually breaking anything.

Writing it by hand is just faster for something small, and you're not fighting any tooling. But nothing stops the docs from going stale since they're not tied together.

If it were up to me - generate it for anything complex like Warehouse, but for something simple like Store, handwritten's fine. I'd just want some lightweight way to pull docs from the code automatically so they don't
quietly get out of date.
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
I'd start with the actual business rules, since that's where bugs really hide. For Warehouse, I tested stuff like "can't create a duplicate warehouse code" or "can't go over capacity" using fake objects instead of
a real database. Quick to write, quick to run.

Then I'd test the API itself, but only the important paths - checking you get the right response and the right error codes. These are slower since they spin up the whole app, so I wouldn't go overboard here.
After that, maybe a couple of full end-to-end tests for the most critical flow, like replacing a warehouse. Just a few, since these are the slowest and priciest to keep working.

With limited time, I'd skip testing every single edge case and just cover the ones that actually matter. And long term, I'd want these running automatically on every change, so it's not just a one-off effort.