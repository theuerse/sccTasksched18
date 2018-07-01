# sccTasksched18



## TODO:

Script/Program as frame which encompasses the whole operation (see approach-diagram) (owner: ?)

Run-Script (read/reorg input for further use (MOHEFT), run MOHEFT + calc hypervolume) (owner: ?)

MOHEFT impl. (owners: Sebastian, seems ok so far)

Hypervolume calculation (owner: Sebastian, looking ok so far)

Optimum calculation (owner: ?)

Evaluation (comparison of Hypervolumes with optimum) (owner: ?)


### Remarks by Sebastian:
Currently, there is not much non-determinism in MOHEFT, so the result stays the same in subsequent runs. 
(B-rank could have random-selection as tie-breaker, but in our example, the are no ties to break).




### ETC:
one Hypervolume per run (result)
because of GA -> use multiple runs as a run may yield different results (from others)
use hypervolume to make results comparable  -> select best one to be compared to optimum

