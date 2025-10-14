package cloud.mallne.dicentra.areaassist.model.parcel.domain

interface ParcelCrateDomain<Landmark : LandmarkDomain, Task : TaskDomain, Note : NoteDomain, LandUsage : LandUsageDomain, ParcelProperty : ParcelPropertyDomain> {
    var parcel: ParcelDomain
    var landMarks: List<Landmark>
    var tasks: List<Task>
    var notes: List<Note>
    var landUsages: List<LandUsage>
    var properties: List<ParcelProperty>
}