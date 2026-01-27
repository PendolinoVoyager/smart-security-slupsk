'use client' // Error boundaries must be Client Components
 
import { HttpError } from '@/api/utils'
import { Button } from '@/components/ui/button'
import { Card, CardFooter } from '@/components/ui/card'
import { getAuthData } from '@/lib/auth/client'
import { Separator } from 'radix-ui'
import { useEffect } from 'react'
 
export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string }
  reset: () => void
}) {
//   const router = useRouter();
  useEffect(() => {
    console.error(error)
  }, [error])
  
  return (
    <Card className='pb-50 flex flex-column justify-evenly align-items-center'>
    <div className='flex justify-evenly align-center'>
      <h2 className='font-bold'>Something went wrong!</h2>
      <Separator.Root className="SeparatorRoot" orientation='vertical' />
      <p>Cannot access page - {error.message}</p>
    </div>
        
            <Button
            className='w-100 center'
            onClick={
                () => reset()
            }
            >
            Try again
            </Button>
      </Card>
  )
}