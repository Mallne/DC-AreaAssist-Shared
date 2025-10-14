package cloud.mallne.dicentra.areaassist.model.parcel.domain

interface ParcelCrateDomain<out Parcel : ParcelDomain, out Landmark : LandmarkDomain, out Task : TaskDomain, out Note : NoteDomain, out LandUsage : LandUsageDomain, out ParcelProperty : ParcelPropertyDomain> {
    val parcel: Parcel
    val landMarks: List<Landmark>
    val tasks: List<Task>
    val notes: List<Note>
    val landUsages: List<LandUsage>
    val properties: List<ParcelProperty>
}