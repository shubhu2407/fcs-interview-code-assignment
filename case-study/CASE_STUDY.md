# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**
The hard part is that costs like labor and transport are often shared across multiple Warehouses or Stores, so you need a fair way to split them - by volume, by space, something like that. I'd track costs against
businessUnitCode, same as we use for Warehouses, so history follows the unit even if the Warehouse itself gets replaced later.

Questions I'd ask: how often do costs need updating - real time or daily? Who owns this data today? And should a replaced Warehouse keep its old cost history or start fresh?

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**
I'd look at capacity usage first - if a location's Warehouses are always near their max capacity, or always way under, that's a clear sign something needs to change. I'd prioritize whatever costs the most and is
easiest to fix, and always measure before and after to know it worked.

Questions I'd ask: what's actually driving the cost - space, labor, transport? Is there a baseline already tracked? How much disruption is okay while testing changes?

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**
If cost data lives only inside this system and finance uses something else, you end up with two versions of the truth, and someone's manually reconciling spreadsheets every month. That's slow and error-prone. Real
integration means the numbers match automatically, and finance can trust what they're seeing without double-checking it.

For this to work cleanly, I'd want a clear, well-defined way for the systems to talk to each other, not just a one-off export. And I'd want to think about what happens if the sync fails partway through - you don't
want half the data updated and half not.

Questions I'd ask: What financial system are we integrating with, and does it expose a proper API, or are we stuck with file exports? Does it need to
be real-time, or is a scheduled sync (nightly, hourly) good enough? And who should get alerted if the sync fails?

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**
Good forecasting needs solid historical data to look back on, so this really depends on Scenario 1 being done well first - if costs aren't tracked accurately, forecasts built on top of them won't be reliable
either. 

Budgets to be set at the same level we already track things - per Warehouse or per Location - so people can compare planned versus actual spend at a level that actually means something operationally.

Questions I'd ask: How far ahead does the business actually need to forecast - a month, a quarter, a year?

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**
This one connects directly to something we actually built - when a Warehouse is replaced, we archive the old one and create a new one with the same businessUnitCode, so there's a continuous history under that one
code. Preserving that cost history matters because otherwise a replacement looks like the business unit's costs just reset to zero, when really it's
the same operation continuing under a new physical Warehouse.

Keeping that history also helps with budget continuity - if the new Warehouse is meant to operate within roughly the same budget as the old one, you need the old numbers to actually compare against. Without that,
you can't tell if the replacement was worth it or if costs quietly crept up.

Questions I'd ask: Should the new Warehouse's budget be set based on the old one's actuals, or reset independently? Is there a transition period where both the old costs and new costs need to be tracked together?

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
