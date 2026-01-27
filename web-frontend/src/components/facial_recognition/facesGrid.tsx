import { FaceTile } from "./faceTile";

type Face = {
  id: number;
  name: string;
  imageUrl: string;
};

type FacesGridProps = {
  faces: Face[];
  loading: boolean;
  error?: string;
};

export function FacesGrid({ faces, loading, error }: FacesGridProps) {
  if (error) {
    return <p role="alert">Error loading faces: {error}</p>;
  }

  if (loading) {
    return <p>Loading saved faces...</p>;
  }

  if (faces.length === 0) {
    return <p>No saved faces for this device.</p>;
  }

  return (
    <section className="flex flex-col gap-4">
      <h3 className="text-lg font-medium">Saved Faces</h3>

      <div
        className="
          grid gap-4
          grid-cols-[repeat(auto-fill,minmax(12rem,1fr))]
        "
      >
        {faces.map((face) => (
          <FaceTile
            key={face.id}
            face={face}
          />
        ))}
      </div>
    </section>
  );
}
