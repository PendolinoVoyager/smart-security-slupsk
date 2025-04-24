import Image from "next/image";

export default function Home() {
  return (
    <div className="space-y-4">
      <h1 className="text-3xl font-bold">
        Ladna strona glowna dla niezalogowanych
      </h1>
      <p className="text-gray-600">
        A jak zalogowany to dashboard z domyslnym urzadzeniem / lista urzadzen
      </p>
    </div>
  );
}
