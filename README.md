# sccTasksched18



## TODO:

Script/Program as frame which encompasses the whole operation (see approach-diagram) (owner: Sebastian)

Run-Script (read/reorg input for further use (MOHEFT), run MOHEFT + calc hypervolume) (owner: Sebastian)

MOHEFT impl. (owners: Sebastian, seems ok so far)

Hypervolume calculation (owner: Sebastian, looking ok so far)

Optimum calculation (owner: Sebastian, ok if we consider Utopia-point)

Evaluation (comparison of Hypervolumes with optimum) (owner: Sebastian)

Documentation (owner: Sebastian + ?)


### Remarks by Sebastian:
Currently, there is not much non-determinism in MOHEFT, so the result stays the same in subsequent runs. 
(B-rank could have random-selection as tie-breaker, but in our example, the are no ties to break).

Optimum-calculation by considering utopia-point? Currently: utopia at hv of 11.6743250823 (between utopia
and nadir) VS our MOHEFT at hv of 9.6785855305 (between MOHEFT pareto-front and nadir point).


### ETC:
one Hypervolume per run (result)
because of GA -> use multiple runs as a run may yield different results (from others)
use hypervolume to make results comparable  -> select best one to be compared to optimum

