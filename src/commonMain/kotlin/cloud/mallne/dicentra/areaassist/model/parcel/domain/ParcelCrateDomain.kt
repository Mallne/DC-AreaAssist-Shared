package cloud.mallne.dicentra.areaassist.model.parcel.domain

interface ParcelCrateDomain<Parcel : ParcelDomain, Landmark : LandmarkDomain, Task : TaskDomain, Note : NoteDomain, LandUsage : LandUsageDomain, ParcelProperty : ParcelPropertyDomain> {
    val parcel: Parcel
    val landMarks: List<Landmark>
    val tasks: List<Task>
    val notes: List<Note>
    val landUsages: List<LandUsage>
    val properties: List<ParcelProperty>

    fun clone(
        parcel: Parcel = this.parcel,
        landMarks: List<Landmark> = this.landMarks,
        tasks: List<Task> = this.tasks,
        notes: List<Note> = this.notes,
        landUsages: List<LandUsage> = this.landUsages,
        properties: List<ParcelProperty> = this.properties,
    ): ParcelCrateDomain<Parcel, Landmark, Task, Note, LandUsage, ParcelProperty>
}