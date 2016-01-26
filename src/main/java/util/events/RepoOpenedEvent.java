package util.events;

public class RepoOpenedEvent extends UnusedStoredReposChangedEvent {
    public final String repoId;
    public final boolean isPrimaryRepo;

    public RepoOpenedEvent(String repoId, boolean isPrimaryRepo) {
        this.repoId = repoId;
        this.isPrimaryRepo = isPrimaryRepo;
    }
}
