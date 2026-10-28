# Integration and issue resolution

The supplied Task 3/4 code was provided in the conversation, not already installed in the workspace. Its lesson/field locks, actor-aware operations, future attributes, and experience history were integrated with the existing Foundations files. Unrelated account code was retained. The controller file is `ControllerLessons.java`, matching its public class, even though the supplied heading said ControlLessons.java.

The following corrections were necessary to test the supplied Contributor story rather than merely the previous CRUD implementation:

| Supplied gap | Implemented correction | Executable evidence |
| --- | --- | --- |
| Single-record read accepted only an ID | Actor-aware reads reject foreign IDs; controller uses the authorized overload | C02 named tests |
| Other-role additions had no protected read projection | Contributor reads/lists omit search terms, numeric statistics, and visibility metadata; core writes cannot overwrite them | C06 named tests |
| Future attributes existed only in memory | Database stores metadata separately, maps it on reads, and preserves it during core updates | C12 restart scenarios |
| CREATE TABLE alone did not upgrade older HW2 databases | Idempotent ALTER TABLE additions preserve existing lesson rows | Repeated connection/restart runs; schema code review |
| Lock buttons had empty captions; locked fields lacked an explicit textual indicator | Admin buttons show Lock/Unlock; contributor fields show [locked] and are noneditable; model rejects programmatic changes | C04 and screenshot 09 |
| History append/read existed only below the GUI | Add experience and Experience history buttons use authorized model operations and retain multiple attempts | C07 and screenshot 10 |
| Experience rows could outlive a deleted lesson | New history table references the lesson with delete cascade; entity append refuses nonexistent lessons | Persistence schema; entity/history scenarios |

Trust boundary: Database and actorless entity/model helpers are internal storage and fixture APIs. Contributor GUI actions call the actor-aware model methods. Authentication itself is not tested by the identity fixtures. Admin represents other privileged roles in TP2; a dedicated curator implementation remains future work.

The future metadata currently represented is searchTerms, timeHours, effortHours, and statsVisible. It is deliberately absent from the contributor projection even when the stored future visibility flag is true, because the supplied Phase Two story prohibits showing other-role additions. A Phase Three visibility policy must be agreed with stakeholders before exposing such information.

No AI tool, AI coaching provider, or automatic time/effort aggregation was implemented. Team-specific requirements from Ed Discussions and the AICoaching plan still need team confirmation. The production changes also mean teammates should regenerate their Task 3/4 Javadoc and source screenshots from the integrated source before submitting those separate deliverables.
